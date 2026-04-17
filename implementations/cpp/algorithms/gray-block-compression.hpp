#pragma once

#include <vector>
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../image/metadata.hpp"
#include "../fractal/block/block.hpp"
#include "../fractal/block/reduction/reduction-strategy.hpp"
#include "../utils/matrix.hpp"

using namespace std;

class GrayBlockCompression {
  public:
    GrayBlockCompression(int rbd, int dbd, int parallelism, ReductionStrategy rs);
    vector<FractalMapping> compress(PGMAImageMetadata metadata);

  private:
    // RANGE BLOCK DIMENSION
    int RBD;

    // DOMAIN BLOCK DIMENSION
    int DBD;

    // number of threads to use for compression.
    int parallelism;

    int image_width;
    int image_height;
    Matrix<GrayPixel> image_pixels;

    Matrix<Block> domain_blocks;
    Matrix<Block> range_blocks;
    Matrix<Block> reduced_domain_blocks;

    ReductionStrategy reductionStrategy;


    void build_domains();
    void build_ranges();
    void build_reduced_domains();
    vector<FractalMapping> build_fractal_mappings();
    float calculate_s(Block& range_block, Block& reduced_domain_block, float mean_r, float mean_d);
    float calculate_o(float mean_r, float mean_d, float s);

};
