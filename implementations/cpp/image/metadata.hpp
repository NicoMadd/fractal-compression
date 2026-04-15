#pragma once

#include "../pixel/pixel.hpp"
#include "./sequence-reader.hpp"
#include <vector>

using namespace std;

class PGMAImageMetadata
{
  
  public:
    int height;
    int maxVal;
    int width;
    vector<vector<GrayPixel>> pixels;

    PGMAImageMetadata(ifstream &ifs);
  
  
  private:

    SequenceReader sr;

    void readMagicNumber();
    void skipComment();
    void readPixel(vector<GrayPixel>& bucket);
};

PGMAImageMetadata loadImage(string imagePath);
