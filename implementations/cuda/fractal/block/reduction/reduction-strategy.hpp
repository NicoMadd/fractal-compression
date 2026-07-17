#pragma once

#include "../../../pixel/pixel.hpp"
#include "../../../utils/matrix.hpp"

#include <string>

using namespace std;

class ReductionStrategy {
    public:
        virtual ~ReductionStrategy() = default;
        virtual Matrix<GrayPixel>* reduce(Matrix<GrayPixel>* pixels, int reduceTo) = 0;
        virtual string description() const { return "reducing"; }
};


class MeanReductionStrategy : public ReductionStrategy {
    public:
        string description() const override { return "mean-reducing"; }
        Matrix<GrayPixel>* reduce(Matrix<GrayPixel>* pixels, int reduceTo) override;
    private:
        Matrix<float> avgKernel(int dim);
};
