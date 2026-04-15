#pragma once

#include "../pixel/pixel.hpp"
#include "./sequence-reader.hpp"
#include <vector>

using namespace std;

struct ImageMetadata
{
  int width;
  int height;
  vector<vector<GrayPixel>> pixels;
};

class PGMAImageMetadata
{
  
  public:
    int height;
    int maxVal;
    int width;
    PGMAImageMetadata(ifstream &ifs);
  
  
  private:

    SequenceReader sr;

    void readMagicNumber();
    void skipComment();
};

ImageMetadata loadImage(string imagePath);
