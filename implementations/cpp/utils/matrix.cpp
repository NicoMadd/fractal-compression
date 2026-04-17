#include "./matrix.hpp"
#include "../fractal/block/block.hpp"
#include "../pixel/pixel.hpp"

#include <vector>

using namespace std;


template<typename T>
Matrix<T>::Matrix(int rows, int cols) : rows(rows), cols(cols){
    data.resize(rows);
    for(int i=0;i<rows;i++){
        data[i].resize(cols);
    }
}

template<typename T>
Matrix<T>::Matrix(vector<vector<T>> _data) : data(_data), rows(_data.size()), cols(_data[0].size()){}

template<typename T>
void Matrix<T>::set(int row, int col, T value){
    data[row][col] = value;
}

template<typename T>
T Matrix<T>::get(int row, int col){
    return data[row][col];
}

template<typename T>
int Matrix<T>::getRows(){
    return rows;
}

template<typename T>
int Matrix<T>::getCols(){
    return cols;
}

template<typename T>
vector<vector<T>> Matrix<T>::getData(){
    return data;
}
