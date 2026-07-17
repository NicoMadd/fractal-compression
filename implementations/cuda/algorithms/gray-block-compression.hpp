#pragma once

#include <vector>
#include "../fractal/mapping/fractal-mapping.hpp"
#include "../image/metadata.hpp"
#include "../fractal/block/block.hpp"
#include "../fractal/block/reduction/reduction-strategy.hpp"
#include "../utils/matrix.hpp"
#include "concurrent/domain-finder.hpp"

using namespace std;

class GrayBlockCompression {
  public:
    GrayBlockCompression(int rbd, int dbd, int parallelism, ReductionStrategy* rs);
    vector<FractalMapping>* compress(PGMAImageMetadata metadata);
    int range_size();
    int domain_size();
    ReductionStrategy* get_reduction_strategy();

  private:
    // RANGE BLOCK DIMENSION
    int RBD;

    // DOMAIN BLOCK DIMENSION
    int DBD;

    // number of threads to use for compression.
    int parallelism;

    int image_width;
    int image_height;
    Matrix<GrayPixel>* image_pixels = nullptr;

    Matrix<Block> domain_blocks;
    Matrix<Block> range_blocks;
    Matrix<Block> reduced_domain_blocks;

    ReductionStrategy* reduction_strategy;

    void build_domains();
    void build_ranges();
    void build_reduced_domains();
    void finalize_range_match(const FractalMapping& mapping,
                              vector<FractalMapping>* fractal_mappings, int& done, int total_ranges, int step);
    vector<FractalMapping>* build_fractal_mappings();

};
