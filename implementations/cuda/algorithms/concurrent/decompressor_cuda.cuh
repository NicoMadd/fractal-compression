#pragma once

#include <cstdint>
#include "../../fractal/mapping/fractal-mapping.hpp"

void launch_decompression_kernel(
    const FractalMapping* d_mappings,
    int num_mappings,
    const uint8_t* d_img,
    uint8_t* d_next,
    int width,
    int domainSize,
    int rangeSize);
