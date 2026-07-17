#pragma once

// For the moment just gray pixel
class GrayPixel {
    public:
        int level;

        GrayPixel() : level(0) {}

        GrayPixel(int level);

};


GrayPixel randomGrayPixel();
