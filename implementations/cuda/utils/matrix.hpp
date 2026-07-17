#pragma once

#include <functional>
#include <utility>
#include <vector>

#include "../pixel/pixel.hpp"

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

    void resize(int newRows, int newCols) {
        rows = newRows;
        cols = newCols;
        data.resize(rows);
        for (int i = 0; i < rows; i++) {
            data[i].resize(cols);
        }
    }

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

template<typename T, typename F>
void fill(Matrix<T>& m, F&& supplier) {
    for (int i = 0; i < m.getRows(); i++) {
        for (int j = 0; j < m.getCols(); j++) {
            m.set(i, j, supplier());
        }
    }
}

template<typename T, typename F>
void fill(Matrix<T>& m, int rows, int cols, F&& supplier) {
    m.resize(rows, cols);
    fill(m, std::forward<F>(supplier));
}

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


void copySquare(Matrix<GrayPixel>* src, Matrix<GrayPixel>* dst, int fromX, int fromY, int dim);
