#include "decompressor.hpp"


#include "../../fractal/block/reduction/reduction-strategy.hpp"
#include "../../utils/matrix.hpp"
#include <cstdint>
#include <cuda_runtime.h>
#include <cuda_runtime_api.h>
#include <driver_types.h>
#include <vector>
#include "decompressor_cuda.cuh"

Decompressor::Decompressor(int domainSize, int rangeSize, ReductionStrategy* reductionStrategy)
    : domainSize_(domainSize),
      rangeSize_(rangeSize),
      reductionStrategy_(reductionStrategy) {}

Decompressor::~Decompressor() {
    cudaFree(d_img_);
    cudaFree(d_next_);
    cudaFree(d_mappings_);
}


void Decompressor::ensure_gpu_buffers(int h, int w, size_t num_mappings){
    if (!gpu_initialized_) {
        cudaMalloc(&d_img_, h * w);
        cudaMalloc(&d_next_, h * w);
        cudaMalloc(&d_mappings_, num_mappings * sizeof(FractalMapping));
    
        gpu_initialized_ = true;
    }
}


void Decompressor::apply(const vector<FractalMapping>* mappings, Matrix<GrayPixel>& img, Matrix<GrayPixel>& next) {
  

    // allocate GPU memory
    ensure_gpu_buffers(img.getRows(), img.getCols(), mappings->size());
   
    // copy img into d_img_
    const int h = img.getRows();
    const int w = img.getCols();
    const size_t n = h * w;

    std::vector<uint8_t> host(n);
    for(int r = 0; r<h;r++){
        for(int c = 0; c<w;c++){
            host[r*w+c] = img.get(r, c).level;
        }
    }

    // load img into gpu global mem
    cudaMemcpy(d_img_, host.data(), n, cudaMemcpyHostToDevice);
    // load mappings into gpu global mem
    cudaMemcpy(d_mappings_, mappings->data(), mappings->size() * sizeof(FractalMapping), cudaMemcpyHostToDevice);

    // run mappings.length times grid. one for each mapping.
    //      the thread uses its index to fetch the mapping.
    launch_decompression_kernel(d_mappings_, mappings->size(), d_img_, d_next_, w, domainSize_, rangeSize_);

    // load back into host
    cudaMemcpy(host.data(), d_next_, n, cudaMemcpyDeviceToHost);
    for (int r = 0; r < h; r++)
        for (int c = 0; c < w; c++)
            next.set(r, c, GrayPixel(host[r * w + c]));
}
