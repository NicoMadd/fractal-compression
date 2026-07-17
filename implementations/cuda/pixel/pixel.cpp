#include "./pixel.hpp"
#include <cstdlib>
#include <algorithm>



GrayPixel::GrayPixel(int level){
    this->level=level;
}


GrayPixel randomGrayPixel(){
    // Generate a random gray level (0-255)
    int level = rand() % 256;
    // Clamp gray level to [0, 255] using std::min and std::max
    level = std::max(0, std::min(255, level));
    return GrayPixel(level);
}
