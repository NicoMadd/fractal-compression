#include "decompressor.hpp"

#include <algorithm>

#include "../../fractal/block/reduction/reduction-strategy.hpp"
#include "../../fractal/transformation/transformation.hpp"
#include "../../utils/matrix.hpp"

Decompressor::Decompressor(int domainSize, int rangeSize, ReductionStrategy* reductionStrategy)
    : domainSize_(domainSize), rangeSize_(rangeSize), reductionStrategy_(reductionStrategy) {}

void Decompressor::apply(const FractalMapping& mapping, Matrix<GrayPixel>& img, Matrix<GrayPixel>& next) {
    Matrix<GrayPixel> domainPixels;
    domainPixels.resize(domainSize_, domainSize_);

    copySquare(&img, &domainPixels, mapping.domain_x, mapping.domain_y, domainSize_);

    Matrix<GrayPixel>* reducedDomain = reductionStrategy_->reduce(&domainPixels, rangeSize_);

    float s = mapping.s;
    float o = mapping.o;
    TransformationType transformation_type = mapping.transformation_type;
    Transformation* transformation = Transformation::from(transformation_type);
    Matrix<GrayPixel> transformedDomain;
    transformedDomain.resize(reducedDomain->getRows(), reducedDomain->getCols());
    transformation->transform(reducedDomain, &transformedDomain);

    for (int i = 0; i < reducedDomain->getRows(); i++) {
        for (int j = 0; j < reducedDomain->getCols(); j++) {
            GrayPixel gp = transformedDomain.get(i, j);

            int newLevel = static_cast<int>(s * static_cast<float>(gp.level) + o);
            newLevel = std::max(0, std::min(255, newLevel));

            GrayPixel newGrayPixel = GrayPixel(newLevel);

            next.set(mapping.range_x + i, mapping.range_y + j, newGrayPixel);
        }
    }
}
