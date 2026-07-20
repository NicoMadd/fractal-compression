#pragma once

#include "../../fractal/mapping/fractal-mapping.hpp"
#include "../../pixel/pixel.hpp"
#include "../../utils/matrix.hpp"
#include "../../fractal/block/reduction/reduction-strategy.hpp"




/** One mapping step for PGMA decode: domain tile, reduce, affine s/o, write range (Java: pipelines.concurrent.Decompressor). */
class Decompressor {
  public:
    Decompressor(int domainSize, int rangeSize, ReductionStrategy* reductionStrategy);
    ~Decompressor();

    void apply(const vector<FractalMapping>* mappings, Matrix<GrayPixel>& img, Matrix<GrayPixel>& next);

  private:
    void ensure_gpu_buffers(int h, int w, size_t num_mappings);

    bool gpu_initialized_ = false;
    uint8_t* d_img_ = nullptr;
    uint8_t* d_next_ = nullptr;
    FractalMapping* d_mappings_ = nullptr;
    int domainSize_;
    int rangeSize_;
    ReductionStrategy* reductionStrategy_;
};
