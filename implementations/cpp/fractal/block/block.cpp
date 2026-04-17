#pragma once

#include "./block.hpp"

Block::Block(Matrix<GrayPixel>& pixels, int x, int y, int width, int height) : pixels(pixels), x(x), y(y), width(width), height(height){
}


Matrix<GrayPixel> Block::reduce(int reduceTo, ReductionStrategy rs){
    rs.reduce(this->pixels, reduceTo);
}

float Block::mean(){
    float sum = 0;
    int denominator = this->width * this->height;
    for(int i = 0; i < this->width; i++){
        for(int j = 0; j < this->height; j++){
            sum += this->pixels.get(i, j).level;
        }
    }
    return sum / denominator;
}

GrayPixel Block::get(int x, int y){
    this->pixels.get(x, y);
}

