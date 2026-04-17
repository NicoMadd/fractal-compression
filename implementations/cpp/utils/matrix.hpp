#pragma once


#include <vector>

using namespace std;


template<typename T>
class Matrix {
    public:
        Matrix() : rows(0), cols(0){}
        Matrix(int rows, int cols);
        Matrix(vector<vector<T>> data);
        void set(int row, int col, T value);
        T get(int row, int col);
        int getRows();
        int getCols();
        vector<vector<T>> getData();
    private:
        vector<vector<T>> data;
        int rows;
        int cols;

};

