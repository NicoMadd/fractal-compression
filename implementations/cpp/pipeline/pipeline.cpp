#include "pipeline.hpp"

#include <chrono>
#include <iomanip>
#include <iostream>
#include <vector>

#include "../utils/run_logging.hpp"

namespace {
  double fmtMs(double seconds) {
      return seconds * 1000.0;
  }
}

PGMAPipeline::PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc) : metadata(metadata), gbc(gbc) {
}

void PGMAPipeline::run() {
  const std::chrono::steady_clock::time_point compression_start =
      std::chrono::steady_clock::now();
  std::vector<FractalMapping> fractalMappings = this->compress();
  const std::chrono::steady_clock::time_point compression_end =
      std::chrono::steady_clock::now();
  const std::chrono::steady_clock::duration elapsed = compression_end - compression_start;
  const std::chrono::nanoseconds elapsed_ns =
      std::chrono::duration_cast<std::chrono::nanoseconds>(elapsed);
  const double compression_seconds = static_cast<double>(elapsed_ns.count()) * 1e-9;

  std::cout << std::fixed << std::setprecision(2);
  std::cout << "--- Summary ---\n";
  std::cout << "Compression: " << fmtMs(compression_seconds) << " ms\n";
  std::cout << "Range mappings: " << fractalMappings.size() << '\n';

  if (run_logging::is_debug() && !fractalMappings.empty()) {
    FractalMapping& fm = fractalMappings[0];
    std::cout << "First mapping (sample): range (" << fm.range_x << "," << fm.range_y << ") domain ("
              << fm.domain_x << "," << fm.domain_y << ") s=" << fm.s << " o=" << fm.o << '\n';
  }

}

std::vector<FractalMapping> PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

void PGMAPipeline::decompress() {
    run_logging::debug("Starting decompression");
}
