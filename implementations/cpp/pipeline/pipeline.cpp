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
#include "../algorithms/concurrent/decompressor.hpp"
#include "../fractal/mapping/codebook.hpp"
#include "../executors/executors.hpp"
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../utils/paths.hpp"

using namespace std;

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

PGMAPipeline::PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, string runDir,
                           int decompressionIterations, int decompressionParallelism)
    : metadata(metadata), gbc(gbc), runDir(std::move(runDir)),
      decompressionIterations(decompressionIterations),
      decompressionParallelism(decompressionParallelism) {
}

void PGMAPipeline::run() {
  time_util::Stopwatch compression_sw;
  vector<FractalMapping>* fractalMappings = this->compress();
  const double compression_seconds = compression_sw.elapsed_seconds();

  time_util::Stopwatch codebook_sw;
  Codebook codebook(gbc.rangeSize(), gbc.domainSize(), fractalMappings);
  const std::string codebookPath = runDir + "/codebook.fc";
  codebook.save(codebookPath);
  const double codebook_seconds = codebook_sw.elapsed_seconds();

  cout << fixed << setprecision(2);
  cout << "--- Summary ---\n";
  cout << "Compression: " << fmtMs(compression_seconds) << " ms\n";
  cout << "Codebook save: " << fmtMs(codebook_seconds) << " ms\n";
  cout << "Codebook: " << codebookPath << '\n';
  cout << "Range mappings: " << fractalMappings->size() << '\n';

  this->decompress(fractalMappings);
}

vector<FractalMapping>* PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

void PGMAPipeline::decompress(vector<FractalMapping>* fractalMappings) {
    run_logging::debug(
        "Decompression start: image " + to_string(metadata.width) + "x" +
        to_string(metadata.height) + ", " + to_string(fractalMappings->size()) +
        " mappings, " + to_string(decompressionIterations) + " iterations");

    time_util::Stopwatch decompress_sw;

    Matrix<GrayPixel> img;
    Matrix<GrayPixel> next;

    img.resize(metadata.height, metadata.width);
    next.resize(metadata.height, metadata.width);

    fill(img, []{return randomGrayPixel();});
    fill(next, []{return randomGrayPixel();});

    run_logging::debug("Iterating over " + to_string(decompressionIterations) + " iterations");
    run_logging::debug("Decompression: initialized noise buffers");

    const string iterations_dir = runDir + "/iterations";
    file_paths::ensureDirectoryExists(iterations_dir);
    const string benchmark_csv = iterations_dir + "/benchmark.csv";
    ofstream csv(benchmark_csv, ios::out | ios::trunc);
    if (!csv.is_open()) {
      run_logging::error("could not open for write: " + benchmark_csv);
      exit(1);
    }
    csv.imbue(locale::classic());
    csv << "n,timestamp,durationNanos,durationSeconds,mse,mae,psnr\n";

    double last_mse = 0;
    double last_mae = 0;
    double last_psnr = 0;

    Decompressor decompressor(gbc.domainSize(), gbc.rangeSize(), gbc.getReductionStrategy());

    for (int iter = 0; iter <= decompressionIterations; iter++) {
      run_logging::debug("Iteration " + to_string(iter));
      const auto iteration_start = chrono::steady_clock::now();

      if (decompressionParallelism <= 1) {
        for (size_t mi = 0; mi < fractalMappings->size(); mi++) {
          decompressor.apply(fractalMappings->at(mi), img, next);
        }
      } else {
        Executor ex(decompressionParallelism);
        for (size_t mi = 0; mi < fractalMappings->size(); mi++) {
          FractalMapping m = fractalMappings->at(mi);
          ex.submit([&decompressor, m, &img, &next]() { decompressor.apply(m, img, next); });
        }
        ex.shutdown();
        ex.join();
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
    std::cout << "Iterations: " << decompressionIterations << '\n';
    std::cout << "Range mappings: " << fractalMappings->size() << '\n';
    std::cout << "MSE: " << fmt3(last_mse) << "  MAE: " << fmt3(last_mae) << "  PSNR: " << fmt3(last_psnr)
              << '\n';
    std::cout << "Iteration metrics: " << benchmark_csv << '\n';
    std::cout << "Output: " << runDir + "/final.pgm" << '\n';
}
