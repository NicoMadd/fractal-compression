#include "reduction-strategy.hpp"
#include "../../../pixel/pixel.hpp"
#include "../../../utils/matrix.hpp"

using namespace std;

Matrix<GrayPixel>* MeanReductionStrategy::reduce(Matrix<GrayPixel>* pixels, int reduceTo) {

    int rows = pixels->getRows();
    int cols = pixels->getCols();
    Matrix<GrayPixel>* reduced = new Matrix<GrayPixel>(rows / reduceTo, cols / reduceTo);
    Matrix<float> avgKernel = this->avgKernel(reduceTo);

    for(int i = 0; i < rows; i += reduceTo) {
        for(int j = 0; j < cols; j += reduceTo) {
            Matrix<GrayPixel> innerPixels(reduceTo, reduceTo);
            for(int ii = 0; ii < reduceTo; ii++) {
                for(int jj = 0; jj < reduceTo; jj++) {
                    innerPixels.set(ii, jj, pixels->get(i + ii, j + jj));
                }
            }

            Matrix<float> floatInnerPixels = floatMap<GrayPixel>(innerPixels, [](const GrayPixel& pixel) -> float {
                return pixel.level;
            });
            float value = dotProduct(floatInnerPixels, avgKernel);
            reduced->set(i / reduceTo, j / reduceTo, GrayPixel(value));
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
