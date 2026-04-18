#pragma once

#include "../algorithms/gray-block-compression.hpp"
#include "../image/metadata.hpp"
#include <string>
#include <vector>

class PGMAPipeline {
  public:
    PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, std::string runDir);
    void run();
  private:
    PGMAImageMetadata metadata;
    GrayBlockCompression gbc;
    std::string runDir;
    std::vector<FractalMapping> compress();
    void decompress(std::vector<FractalMapping> mapping);
};
