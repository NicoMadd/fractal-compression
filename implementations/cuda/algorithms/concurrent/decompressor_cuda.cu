#include "decompressor_cuda.cuh"
#include <cuda_runtime.h>

__global__ void decompression_kernel(
    const FractalMapping* d_mappings,
    const uint8_t* d_img,
    uint8_t* d_next,
    int width,
    int domainSize,
    int rangeSize) {
    // ...
}

void launch_decompression_kernel(
    const FractalMapping* d_mappings,
    int num_mappings,
    const uint8_t* d_img,
    uint8_t* d_next,
    int width,
    int domainSize,
    int rangeSize) {
    decompression_kernel<<<num_mappings, dim3(rangeSize, rangeSize)>>>(
        d_mappings, d_img, d_next, width, domainSize, rangeSize);
    cudaDeviceSynchronize();
}
