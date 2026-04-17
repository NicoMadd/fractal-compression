#pragma once

#include "../../../pixel/pixel.hpp"
#include "../../../utils/matrix.hpp"

using namespace std;

class ReductionStrategy {
    public:
        virtual ~ReductionStrategy() = default;
        virtual Matrix<GrayPixel> reduce(Matrix<GrayPixel>& pixels, int reduceTo);
};


class MeanReductionStrategy : public ReductionStrategy {
    public:
        Matrix<GrayPixel> reduce(Matrix<GrayPixel>& pixels, int reduceTo) override;
};
