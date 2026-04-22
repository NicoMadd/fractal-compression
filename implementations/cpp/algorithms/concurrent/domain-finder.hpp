#pragma once

#include "../../fractal/block/block.hpp"
#include "../../fractal/mapping/fractal-mapping.hpp"
#include "../../utils/matrix.hpp"

class GrayBlockCompression;

/** Result of searching the reduced-domain grid for one range block (used by finalize / logging). */
struct RangeBlockMatchResult {
    FractalMapping mapping;
    float s_win = 0;
    float o_win = 0;
    float min_error = 0;
    int win_di = -1;
    int win_dj = -1;
    float mean_r = 0;
};

/** Best reduced-domain cell for a range block (Java: algorithms.concurrent.DomainFinder). */
class DomainFinder {
  public:
    DomainFinder(Matrix<Block>& reduced_domain_blocks, GrayBlockCompression& codec);

    RangeBlockMatchResult findBest(const Block& range_block_in);

  private:
    Matrix<Block>& reduced_domain_blocks_;
    GrayBlockCompression& codec_;
};
