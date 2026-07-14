#pragma once

#include "../pixel/pixel.hpp"
#include "../files/readers/sequence-reader.hpp"
#include "../utils/matrix.hpp"

using namespace std;

class PGMAImageMetadata
{
  
  public:
    int width;
    int height;
    int maxVal;
    Matrix<GrayPixel>* pixels = nullptr;

    PGMAImageMetadata(ifstream &ifs);
    PGMAImageMetadata(int width, int height, int maxVal, Matrix<GrayPixel>* pixels) : width(width), height(height), maxVal(maxVal), pixels(pixels){}
    void save(string path);
  
  private:
    void readMagicNumber(SequenceReader &sr);
    void skipComment(SequenceReader &sr);
    void readPixel(SequenceReader &sr, int x, int y);
};

PGMAImageMetadata loadImage(string imagePath);


namespace pgma{
  void save(Matrix<GrayPixel> pixels, string path);
  /** Load a P2 PGM; matrix shape is (height × width) rows×cols, same as {@link PGMAImageMetadata(ifs)}. */
  Matrix<GrayPixel> read_matrix(const std::string& path);
};
