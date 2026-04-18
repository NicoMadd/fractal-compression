#include "reduction-strategy.hpp"
#include "../../../pixel/pixel.hpp"
#include "../../../utils/matrix.hpp"

using namespace std;

Matrix<GrayPixel>* MeanReductionStrategy::reduce(Matrix<GrayPixel>* pixels, int reduceTo) {

    int reduceRatio = pixels->getRows() / reduceTo;
    Matrix<GrayPixel>* reduced = new Matrix<GrayPixel>(reduceTo, reduceTo);
    Matrix<float> avgKernel = this->avgKernel(reduceRatio);

    for (int ri = 0; ri < reduceTo; ri++) {
        for (int rj = 0; rj < reduceTo; rj++) {
            int offI = ri * reduceRatio;
            int offJ = rj * reduceRatio;
            Matrix<GrayPixel> innerPixels(reduceRatio, reduceRatio);
            copySquare(pixels, &innerPixels, offI, offJ, reduceRatio);

            Matrix<float> floatInnerPixels = floatMap<GrayPixel>(innerPixels, [](const GrayPixel& pixel) -> float {
                return pixel.level;
            });
            float value = dotProduct(floatInnerPixels, avgKernel);
            reduced->set(ri, rj, GrayPixel(value));
        }
    }
    return reduced;
}


Matrix<float> MeanReductionStrategy::avgKernel(int dim) {
    Matrix<float> kernel(dim, dim);
    float value = 1.0f / (dim * dim);
    for(int i = 0; i < dim; i++) {
        for(int j = 0; j < dim; j++) {
            kernel.set(i, j, value);
        }
    }
    return kernel;
}
