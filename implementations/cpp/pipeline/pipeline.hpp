#pragma once

#include "../algorithms/gray-block-compression.hpp"
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../image/metadata.hpp"
#include <string>
#include <vector>

using namespace std;

class PGMAPipeline {
  public:
    PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, string runDir,
                   int decompressionIterations, int decompressionParallelism);
    void run();
  private:
    PGMAImageMetadata metadata;
    GrayBlockCompression gbc;
    string runDir;
    int decompressionIterations;
    int decompressionParallelism;

    vector<FractalMapping>* compress();
    void decompress(vector<FractalMapping>* fractalMappings);
};
