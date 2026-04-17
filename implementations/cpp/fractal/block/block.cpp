#include "./block.hpp"
#include "./reduction/reduction-strategy.hpp"

Block::Block() : x(0), y(0), width(0), height(0), pixels(nullptr) {}

Block::Block(Matrix<GrayPixel>* pixels, int x, int y, int width, int height)
    : x(x), y(y), width(width), height(height), pixels(pixels) {}

Matrix<GrayPixel>* Block::reduce(int reduceTo, ReductionStrategy* rs){
    return rs->reduce(this->pixels, reduceTo);
}

float Block::mean(){
    float sum = 0;
    int denominator = this->width * this->height;
    for(int i = 0; i < this->width; i++){
        for(int j = 0; j < this->height; j++){
            sum += this->pixels->get(i, j).level;
        }
    }
    return sum / denominator;
}

GrayPixel Block::get(int x, int y){
    return this->pixels->get(x, y);
}

