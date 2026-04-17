#pragma once


#include "reduction/reduction-strategy.hpp"
#include "../../utils/matrix.hpp"
class Block{
    public:
        int x;
        int y;
        int width;
        int height;
        Matrix<GrayPixel>& pixels;
        Block(Matrix<GrayPixel>& pixels, int x, int y, int width, int height);
        Matrix<GrayPixel> reduce(int reduceTo, ReductionStrategy rs);
        float mean();
        GrayPixel get(int x, int y);
};
