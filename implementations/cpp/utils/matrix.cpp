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
