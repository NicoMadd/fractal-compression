#include "matrix.hpp"

using namespace std;

float dotProduct(Matrix<float>& a, Matrix<float>& b) {
    int rows = a.getRows();
    int cols = a.getCols();
    float sum = 0;
    for(int i = 0; i < rows; i++) {
        for(int j = 0; j < cols; j++) {
            sum += a.get(i, j) * b.get(j, i);
        }
    }
    return sum;
}

void copySquare(Matrix<GrayPixel>* src, Matrix<GrayPixel>* dst, int fromX, int fromY, int dim) {
    int srcRows = src->getRows();
    int srcCols = src->getCols();

    for (int i = 0; i < dim; i++) {
        for (int j = 0; j < dim; j++) {
            int finalX = std::min(std::max(fromX + i, 0), srcRows - 1);
            int finalY = std::min(std::max(fromY + j, 0), srcCols - 1);
            GrayPixel value = src->get(finalX, finalY);
            dst->set(i, j, value);
        }
    }
}
