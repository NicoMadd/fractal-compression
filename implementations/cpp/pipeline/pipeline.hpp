#pragma once

#include "../image/metadata.hpp"
#include "../algorithms/gray-block-compression.hpp"
#include <vector>

using namespace std;
class PGMAPipeline {
  public:
    PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc);
    void run();
  private:
    PGMAImageMetadata metadata;
    GrayBlockCompression gbc;
    vector<FractalMapping> compress();
    void decompress();
};
