#pragma once

#include "../../fractal/block/block.hpp"
#include "../../fractal/mapping/fractal-mapping.hpp"
#include "../../utils/matrix.hpp"

class GrayBlockCompression;

/** Best reduced-domain cell for a range block (Java: algorithms.concurrent.DomainFinder). */
class DomainFinder {
  public:
    DomainFinder(Matrix<Block>& reduced_domain_blocks, GrayBlockCompression& codec);

    FractalMapping findBest(const Block& range_block_in);

  private:
    float calculate_s(Block* range, Matrix<GrayPixel>* reduced_domain_pixels, float mean_r, float mean_d,
                      float* out_numerator = nullptr, float* out_denominator = nullptr,
                      bool count_zero_denominator = true);
    float calculate_o(float mean_r, float mean_d, float s);

    Matrix<Block>& reduced_domain_blocks_;
    GrayBlockCompression& codec_;
};

/** LS fits where domain variance was zero (s forced to 0); for debug stats after search. */
long long zero_denominator_pair_count();
