#pragma once
#include <vector>
#include "../pixel/pixel.hpp"

using namespace std;

struct ImageMetadata {
    int width;
    int height;
    vector< vector<GrayPixel> > pixels;
};

ImageMetadata loadImage(string imagePath);
