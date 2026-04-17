#include "pipeline.hpp"

#include <chrono>
#include <iostream>
#include <iomanip>

#include "../utils/run_logging.hpp"
#include "../image/metadata.hpp"
#include "../algorithms/gray-block-compression.hpp"

namespace {
double fmtMs(double seconds) {
    return seconds * 1000.0;
}
}

PGMAPipeline::PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc) : metadata(metadata), gbc(gbc) {
}

void PGMAPipeline::run() {
  using clock = std::chrono::steady_clock;
  auto compression_start = clock::now();
  vector<FractalMapping> fractalMappings = this->compress();
  auto compression_end = clock::now();
  double compression_seconds = std::chrono::duration<double>(compression_end - compression_start).count();

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

vector<FractalMapping> PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

void PGMAPipeline::decompress() {
    run_logging::debug("Starting decompression");
}
