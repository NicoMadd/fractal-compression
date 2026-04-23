#pragma once

#include "../algorithms/gray-block-compression.hpp"
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../image/metadata.hpp"
#include <string>
#include <vector>

using namespace std;

struct DecompressResult {
  double mse = 0;
  double mae = 0;
  double psnr = 0;
};

class PGMAPipeline {
  public:
    PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, string runDir,
                 int decompressionIterations, int decompressionParallelism, bool cleanCodebook,
                 string originalImagePath);
    void run();
  private:
    PGMAImageMetadata metadata;
    GrayBlockCompression gbc;
    string runDir;
    int decompressionIterations;
    int decompressionParallelism;
    bool cleanCodebook;
    /** Path to the input reference PGM; used to re-read pixels for MSE/MAE like Java `recordIterationErrorMetrics`. */
    string originalImagePath;

    vector<FractalMapping>* compress();
    DecompressResult decompress(vector<FractalMapping>* fractalMappings);
};
