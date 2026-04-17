#pragma once

#include "../pixel/pixel.hpp"
#include "./sequence-reader.hpp"
#include "../utils/matrix.hpp"

using namespace std;

class PGMAImageMetadata
{
  
  public:
    int height;
    int maxVal;
    int width;
    Matrix<GrayPixel>* pixels = nullptr;

    PGMAImageMetadata(ifstream &ifs);
  
  
  private:

    SequenceReader sr;

    void readMagicNumber();
    void skipComment();
    void readPixel(int i, int j);
};

PGMAImageMetadata loadImage(string imagePath);
