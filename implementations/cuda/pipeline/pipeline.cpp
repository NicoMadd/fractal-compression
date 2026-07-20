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

#include <filesystem>

#include "../image/error_metrics.hpp"
#include "../utils/run_logging.hpp"
#include "../utils/time.hpp"
#include "../algorithms/concurrent/decompressor.hpp"
#include "../fractal/mapping/codebook.hpp"
#include "../executors/executors.hpp"
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../utils/paths.hpp"
#include "../utils/compression_baselines.hpp"

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
                           int decompressionIterations, int decompressionParallelism, bool cleanCodebook,
                           string originalImagePath)
    : metadata(metadata), gbc(gbc), runDir(std::move(runDir)),
      decompressionIterations(decompressionIterations),
      decompressionParallelism(decompressionParallelism), cleanCodebook(cleanCodebook),
      originalImagePath(std::move(originalImagePath)) {
}

void PGMAPipeline::run() {
  namespace fs = std::filesystem;
  file_paths::copyOriginalToRunDir(this->originalImagePath, this->runDir);
  const std::string codebookPath =
      file_paths::codebookPathForGeometry(runDir, gbc.range_size(), gbc.domain_size());

  time_util::Stopwatch compression_sw;
  vector<FractalMapping>* fractalMappings = nullptr;
  bool loaded_from_disk = false;

  if (!cleanCodebook && fs::exists(codebookPath)) {
    try {
      Codebook on_disk;
      on_disk.read(codebookPath);
      if (on_disk.getRangeSize() == gbc.range_size() && on_disk.getDomainSize() == gbc.domain_size()) {
        fractalMappings = new vector<FractalMapping>(on_disk.getMappings());
        loaded_from_disk = true;
        run_logging::debug("Loaded codebook from " + codebookPath);
      }
    } catch (const std::exception& e) {
      run_logging::debug(string("Codebook load failed, will compress: ") + e.what());
    }
  }

  if (fractalMappings == nullptr) {
    fractalMappings = this->compress();
  }
  const double compression_seconds = compression_sw.elapsed_seconds();

  double codebook_seconds = 0;
  if (!loaded_from_disk) {
    time_util::Stopwatch codebook_sw;
    fs::path cbp(codebookPath);
    if (cbp.has_parent_path()) {
      file_paths::ensureDirectoryExists(cbp.parent_path().string());
    }
    Codebook codebook(gbc.range_size(), gbc.domain_size(), fractalMappings);
    codebook.save(codebookPath);
    codebook_seconds = codebook_sw.elapsed_seconds();
  }

  cout << fixed << setprecision(2);
  cout << "--- Summary ---\n";
  if (loaded_from_disk) {
    cout << "Compression: " << fmtMs(compression_seconds) << " ms (codebook reused)\n";
    cout << "Codebook save: skipped\n";
  } else {
    cout << "Compression: " << fmtMs(compression_seconds) << " ms\n";
    cout << "Codebook save: " << fmtMs(codebook_seconds) << " ms\n";
  }
  cout << "Codebook: " << codebookPath << '\n';
  cout << "Range mappings: " << fractalMappings->size() << '\n';

  const DecompressResult dr = this->decompress(fractalMappings);
  compression_baselines::printRunBaselinesAndCompressionRatios(
      this->runDir, codebookPath, this->originalImagePath, this->decompressionIterations, dr.mse, dr.mae, dr.psnr);
}

vector<FractalMapping>* PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

DecompressResult PGMAPipeline::decompress(vector<FractalMapping>* fractalMappings) {
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

    file_paths::cleanAndCreateIterationsDir(runDir);
    const string iterations_dir = runDir + "/iterations";
    const string benchmark_csv = iterations_dir + "/benchmark.csv";
    ofstream csv(benchmark_csv, ios::out | ios::trunc);
    if (!csv.is_open()) {
      run_logging::error("could not open for write: " + benchmark_csv);
      exit(1);
    }
    csv.imbue(locale::classic());
    csv << "n,timestamp,durationNanos,durationSeconds,mse,mae,psnr\n";

    DecompressResult out;

    Decompressor decompressor(gbc.domain_size(), gbc.range_size(), gbc.get_reduction_strategy());

    for (int iter = 0; iter <= decompressionIterations; iter++) {
      run_logging::debug("Iteration " + to_string(iter));
      const auto iteration_start = chrono::steady_clock::now();


      decompressor.apply(fractalMappings, img, next);
 

      const auto iteration_end = std::chrono::steady_clock::now();
      const int64_t duration_nanos = std::chrono::duration_cast<std::chrono::nanoseconds>(
                                           iteration_end - iteration_start)
                                           .count();
      const int64_t timestamp_nanos =
          std::chrono::duration_cast<std::chrono::nanoseconds>(iteration_end.time_since_epoch()).count();
      const double duration_seconds = static_cast<double>(duration_nanos) / 1e9;

      const std::string iterFrameBase = iterations_dir + "/iter_" + std::to_string(iter);
      pgma::save(next, iterFrameBase);
      run_logging::debug("Decompression wrote intermediate " + iterFrameBase + ".pgm");

      const ImageErrorMetrics err = computeErrorMetricsFromPgmPaths(
          this->originalImagePath, iterFrameBase + ".pgm");
      out.mse = err.mse;
      out.mae = err.mae;
      out.psnr = psnrFromMse(err.mse);
      run_logging::debug("MSE: " + fmt3(err.mse));
      run_logging::debug("MAE: " + fmt3(err.mae));
      run_logging::debug("PSNR: " + fmt3(out.psnr));

      std::cout << "Iteration " << iter << "  MSE: " << fmt3(err.mse) << "  MAE: " << fmt3(err.mae)
                << "  PSNR: " << fmt3(out.psnr) << '\n';

      csv << iter << ',' << timestamp_nanos << ',' << duration_nanos << ','
          << std::setprecision(12) << duration_seconds << ','
          << std::setprecision(12) << err.mse << ','           << std::setprecision(12) << err.mae << ','
          << std::setprecision(12) << out.psnr << '\n';

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
    std::cout << "Iteration metrics: " << benchmark_csv << '\n';
    std::cout << "Output: " << runDir + "/final.pgm" << '\n';
    return out;
}
