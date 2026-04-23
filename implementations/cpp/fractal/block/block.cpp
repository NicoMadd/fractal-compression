#include "./block.hpp"
#include "./reduction/reduction-strategy.hpp"

Block::Block() : x(0), y(0), width(0), height(0), pixels(nullptr) {}

Block::Block(Matrix<GrayPixel>* pixels, int x, int y, int width, int height)
    : x(x), y(y), width(width), height(height), pixels(pixels) {}


/**
 * @brief Reduce the block to a smaller size using the reduction strategy.
 * 
 * @param reduceTo The size to reduce the block to.
 * @param rs The reduction strategy to use.
 * @return Matrix<GrayPixel>* The reduced block.
 */
Matrix<GrayPixel>* Block::reduce(int reduceTo, ReductionStrategy* rs){
    Matrix<GrayPixel> blockPixels(this->width, this->height);
    // FIXME assuming square when selection block pixels.
    copySquare(this->pixels,&blockPixels,this->x,this->y, this->width);
    return rs->reduce(&blockPixels, reduceTo);
}

/**
 * @brief Calculate the mean of the block.
 * 
 * @return float The mean of the block.
 */
float Block::mean(){
    float sum = 0;
    int denominator = this->width * this->height;
    const bool local = pixels && pixels->getRows() == height && pixels->getCols() == width;
    for (int i = 0; i < this->width; i++) {
        for (int j = 0; j < this->height; j++) {
            sum += (local ? pixels->get(i, j) : pixels->get(this->x + i, this->y + j)).level;
        }
    }
    return sum / denominator;
}

/**
 * @brief Get a pixel from the block.
 * 
 * @param x The x coordinate of the pixel.
 * @param y The y coordinate of the pixel.
 * @return GrayPixel The pixel.
 */
GrayPixel Block::get(int x, int y){
    if (pixels && pixels->getRows() == height && pixels->getCols() == width) {
        return pixels->get(x, y);
    }
    return pixels->get(this->x + x, this->y + y);
}

