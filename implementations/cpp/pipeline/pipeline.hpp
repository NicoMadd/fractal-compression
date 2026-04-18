#pragma once

#include "../algorithms/gray-block-compression.hpp"
#include "../image/metadata.hpp"
#include <vector>

class PGMAPipeline {
  public:
    PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc);
    void run();
  private:
    PGMAImageMetadata metadata;
    GrayBlockCompression gbc;
    std::vector<FractalMapping> compress();
    void decompress();
};
