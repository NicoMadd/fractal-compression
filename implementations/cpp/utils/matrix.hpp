#pragma once

#include <vector>
#include <functional>

using namespace std;

template<typename T>
class Matrix {
  public:
    Matrix() : rows(0), cols(0) {}

    Matrix(int rows, int cols) : rows(rows), cols(cols) {
        data.resize(rows);
        for (int i = 0; i < rows; i++) {
            data[i].resize(cols);
        }
    }

    Matrix(vector<vector<T>> _data)
        : data(_data), rows(_data.size()), cols(_data.empty() ? 0 : _data[0].size()) {}
    void set(int row, int col, T value) { data[row][col] = value; }
    T get(int row, int col) { return data[row][col]; }
    int getRows() { return rows; }
    int getCols() { return cols; }
    vector<vector<T>> getData() { return data; }

  private:
    vector<vector<T>> data;
    int rows;
    int cols;
};


float dotProduct(Matrix<float>& a, Matrix<float>& b);

template<typename T>
Matrix<float> floatMap(Matrix<T>& matrix, function<float(const T&)> mapper){
    int rows = matrix.getRows();
    int cols = matrix.getCols();
    Matrix<float> floatMatrix(rows, cols);
    for(int i = 0; i < rows; i++) {
        for(int j = 0; j < cols; j++) {
            floatMatrix.set(i, j, mapper(matrix.get(i, j)));
        }
    }
    return floatMatrix;
}
