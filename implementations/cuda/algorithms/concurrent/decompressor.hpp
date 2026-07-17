#pragma once

#include "../../fractal/mapping/fractal-mapping.hpp"
#include "../../pixel/pixel.hpp"
#include "../../utils/matrix.hpp"

class ReductionStrategy;

/** One mapping step for PGMA decode: domain tile, reduce, affine s/o, write range (Java: pipelines.concurrent.Decompressor). */
class Decompressor {
  public:
    Decompressor(int domainSize, int rangeSize, ReductionStrategy* reductionStrategy);

    void apply(const FractalMapping& mapping, Matrix<GrayPixel>& img, Matrix<GrayPixel>& next);

  private:
    int domainSize_;
    int rangeSize_;
    ReductionStrategy* reductionStrategy_;
};
