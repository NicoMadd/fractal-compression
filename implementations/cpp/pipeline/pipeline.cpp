#include "pipeline.hpp"

#include <algorithm>
#include <chrono>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <locale>
#include <sstream>
#include <string>
#include <vector>

#include "../image/error_metrics.hpp"
#include "../utils/run_logging.hpp"
#include "../utils/time.hpp"
#include "../fractal/mapping/codebook.hpp"
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../utils/paths.hpp"

namespace {
  double fmtMs(double seconds) {
      return seconds * 1000.0;
  }

  std::string fmt3(double value) {
    std::ostringstream oss;
    oss.imbue(std::locale::classic());
    oss << std::fixed << std::setprecision(3) << value;
    return oss.str();
  }
}

PGMAPipeline::PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, std::string runDir)
    : metadata(metadata), gbc(gbc), runDir(std::move(runDir)) {
}

void PGMAPipeline::run() {
  time_util::Stopwatch compression_sw;
  std::vector<FractalMapping>* fractalMappings = this->compress();
  const double compression_seconds = compression_sw.elapsed_seconds();

  time_util::Stopwatch codebook_sw;
  Codebook codebook(gbc.rangeSize(), gbc.domainSize(), fractalMappings);
  const std::string codebookPath = runDir + "/codebook.fc";
  codebook.save(codebookPath);
  const double codebook_seconds = codebook_sw.elapsed_seconds();

  std::cout << std::fixed << std::setprecision(2);
  std::cout << "--- Summary ---\n";
  std::cout << "Compression: " << fmtMs(compression_seconds) << " ms\n";
  std::cout << "Codebook save: " << fmtMs(codebook_seconds) << " ms\n";
  std::cout << "Codebook: " << codebookPath << '\n';
  std::cout << "Range mappings: " << fractalMappings->size() << '\n';

  this->decompress(fractalMappings);
}

std::vector<FractalMapping>* PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

void PGMAPipeline::decompress(std::vector<FractalMapping>* fractalMappings) {
    constexpr int kDecompressionIterations = 10;

    run_logging::debug(
        "Decompression start: image " + std::to_string(metadata.width) + "x" +
        std::to_string(metadata.height) + ", " + std::to_string(fractalMappings->size()) +
        " mappings, " + std::to_string(kDecompressionIterations) + " iterations");

    time_util::Stopwatch decompress_sw;

    Matrix<GrayPixel> img;
    Matrix<GrayPixel> next;

    img.resize(metadata.height, metadata.width);
    next.resize(metadata.height, metadata.width);

    fill(img, []{return randomGrayPixel();});
    fill(next, []{return randomGrayPixel();});

    run_logging::debug("Decompression: initialized noise buffers");

    const std::string iterations_dir = runDir + "/iterations";
    file_paths::ensureDirectoryExists(iterations_dir);
    const std::string benchmark_csv = iterations_dir + "/benchmark.csv";
    std::ofstream csv(benchmark_csv, std::ios::out | std::ios::trunc);
    if (!csv.is_open()) {
      run_logging::error("could not open for write: " + benchmark_csv);
      std::exit(1);
    }
    csv.imbue(std::locale::classic());
    csv << "n,timestamp,durationNanos,durationSeconds,mse,mae,psnr\n";

    double last_mse = 0;
    double last_mae = 0;
    double last_psnr = 0;

    for (int iter = 0; iter < kDecompressionIterations; iter++) {
      run_logging::debug("Iteration " + std::to_string(iter));
      const auto iteration_start = std::chrono::steady_clock::now();

      for (size_t mi = 0; mi < fractalMappings->size(); mi++) {

        FractalMapping mapping = fractalMappings->at(mi);

        Matrix<GrayPixel> domainPixels; 
        domainPixels.resize(gbc.domainSize(), gbc.domainSize());

        copySquare(&img, &domainPixels, mapping.domain_x, mapping.domain_y, gbc.domainSize());

        Matrix<GrayPixel> newRangePixels = Matrix<GrayPixel>(gbc.rangeSize(),gbc.rangeSize());

        ReductionStrategy* rs = gbc.getReductionStrategy();

        Matrix<GrayPixel>* reducedDomain = rs->reduce(&domainPixels, gbc.rangeSize());

        float s = mapping.s;
        float o = mapping.o;

        for(int i=0;i<reducedDomain->getRows();i++){
          for(int j=0;j<reducedDomain->getCols();j++){
            GrayPixel gp = reducedDomain->get(i, j);

            int newLevel = static_cast<int>(s * static_cast<float>(gp.level) + o);
            newLevel = std::max(0, std::min(255, newLevel));

            GrayPixel newGrayPixel = GrayPixel(newLevel);

            next.set(mapping.range_x+i, mapping.range_y+j, newGrayPixel);
          }
        }
      }

      const auto iteration_end = std::chrono::steady_clock::now();
      const int64_t duration_nanos = std::chrono::duration_cast<std::chrono::nanoseconds>(
                                           iteration_end - iteration_start)
                                           .count();
      const int64_t timestamp_nanos =
          std::chrono::duration_cast<std::chrono::nanoseconds>(iteration_end.time_since_epoch()).count();

      const ImageErrorMetrics err = computeErrorVsReference(*metadata.pixels, next);
      last_mse = err.mse;
      last_mae = err.mae;
      last_psnr = psnrFromMse(err.mse);
      run_logging::debug("MSE: " + fmt3(err.mse));
      run_logging::debug("MAE: " + fmt3(err.mae));
      run_logging::debug("PSNR: " + fmt3(last_psnr));

      std::cout << "Iteration " << iter << "  MSE: " << fmt3(err.mse) << "  MAE: " << fmt3(err.mae)
                << "  PSNR: " << fmt3(last_psnr) << '\n';

      const double duration_seconds = static_cast<double>(duration_nanos) / 1e9;
      csv << iter << ',' << timestamp_nanos << ',' << duration_nanos << ','
          << std::setprecision(12) << duration_seconds << ','
          << std::setprecision(12) << err.mse << ',' << std::setprecision(12) << err.mae << ','
          << std::setprecision(12) << last_psnr << '\n';

      const std::string nextPath = runDir + "/next_" + std::to_string(iter);

      pgma::save(next, nextPath);
      run_logging::debug("Decompression wrote intermediate " + nextPath + ".pgm");
      swap(img, next);
    }

    const std::string finalBase = runDir + "/final";
    pgma::save(img, finalBase);
    run_logging::debug("Decompression wrote " + finalBase + ".pgm");

    const double decompress_seconds = decompress_sw.elapsed_seconds();

    std::cout << std::fixed << std::setprecision(2);
    std::cout << "--- Decompression ---\n";
    std::cout << "Time: " << fmtMs(decompress_seconds) << " ms\n";
    std::cout << "Iterations: " << kDecompressionIterations << '\n';
    std::cout << "Range mappings: " << fractalMappings->size() << '\n';
    std::cout << "MSE: " << fmt3(last_mse) << "  MAE: " << fmt3(last_mae) << "  PSNR: " << fmt3(last_psnr)
              << '\n';
    std::cout << "Iteration metrics: " << benchmark_csv << '\n';
    std::cout << "Output: " << runDir + "/final.pgm" << '\n';
}
