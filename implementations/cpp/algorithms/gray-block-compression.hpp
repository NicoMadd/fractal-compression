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
  friend class DomainFinder;
  public:
    GrayBlockCompression(int rbd, int dbd, int parallelism, ReductionStrategy* rs);
    vector<FractalMapping>* compress(PGMAImageMetadata metadata);
    int rangeSize();
    int domainSize();
    ReductionStrategy* getReductionStrategy();

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

    ReductionStrategy* reductionStrategy;

    void build_domains();
    void build_ranges();
    void build_reduced_domains();
    void finalize_range_match(const RangeBlockMatchResult& r, const Block& range_block_for_debug,
                              vector<FractalMapping>* fractal_mappings, int& done, int total_ranges, int step,
                              float& sum_s, float& min_s_agg, float& max_s_agg);
    vector<FractalMapping>* build_fractal_mappings();
    /** Optional {@code out_numerator} / {@code out_denominator} for debug (least-squares accumulators). */
    float calculate_s(Block& range_block, Block& reduced_domain_block, float mean_r, float mean_d,
                        float* out_numerator = nullptr, float* out_denominator = nullptr,
                        bool count_zero_denominator = true);
    float calculate_o(float mean_r, float mean_d, float s);

};
