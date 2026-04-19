#include "reduction-strategy.hpp"
#include "../../../pixel/pixel.hpp"
#include "../../../utils/matrix.hpp"
#include "../../../utils/run_logging.hpp"

#include <string>

using namespace std;

namespace {
bool g_logged_reduction_shape = false;
}

Matrix<GrayPixel>* MeanReductionStrategy::reduce(Matrix<GrayPixel>* pixels, int reduceTo) {

    int rows = pixels->getRows();
    int cols = pixels->getCols();
    int reduceRatio = rows / reduceTo;
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

    if (run_logging::is_debug() && !g_logged_reduction_shape) {
        g_logged_reduction_shape = true;
        const int last = reduceTo - 1;
        run_logging::debug(
            "MeanReductionStrategy::reduce (first call only): input " + to_string(rows) + "x" + to_string(cols)
            + " -> " + to_string(reduceTo) + "x" + to_string(reduceTo) + ", reduceRatio=" + to_string(reduceRatio));
        run_logging::debug(
            "  sample reduced levels: [0,0]=" + to_string(reduced->get(0, 0).level) + " [0," + to_string(last) + "]="
            + to_string(reduced->get(0, last).level) + " [" + to_string(last) + ",0]=" + to_string(reduced->get(last, 0).level)
            + " [" + to_string(last) + "," + to_string(last) + "]=" + to_string(reduced->get(last, last).level));
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
