#pragma once

#include "../algorithms/gray-block-compression.hpp"
#include "../image/metadata.hpp"
#include <string>
#include <vector>

using namespace std;

class PGMAPipeline {
  public:
    PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, string runDir,
                 int decompressionIterations);
    void run();
  private:
    PGMAImageMetadata metadata;
    GrayBlockCompression gbc;
    string runDir;
    int decompressionIterations;
    vector<FractalMapping>* compress();
    void decompress(vector<FractalMapping>* fractalMappings);
};
