#include "metadata.hpp"

#include <fstream>
#include "../utils/utils.hpp"
#include <iostream>

PGMAImageMetadata loadImage(string imagePath) {

  ifstream file(imagePath);

  PGMAImageMetadata metadata = PGMAImageMetadata(file);

  print(metadata.height);
  print(metadata.width);
  print(metadata.maxVal);

  return  metadata;
}


PGMAImageMetadata::PGMAImageMetadata(ifstream &ifs) : sr(ifs), pixels(width, height){
  readMagicNumber();
  sr.readWhitespace();

  skipComment();

  this->width = sr.readNextInt();
  this->height = sr.readNextInt();
  this->maxVal = sr.readNextInt();

  sr.readWhitespace();

  for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
          readPixel(i,j);
      }
  }

}

void PGMAImageMetadata::readPixel(int i, int j){
  int level = sr.readNextInt();

  GrayPixel gp = GrayPixel(level);
  pixels.set(i, j, gp);

}

void PGMAImageMetadata::readMagicNumber(){
  vector<char> magicNumber = sr.read(2);

  // TODO validate equals
  print(magicNumber);
}

void PGMAImageMetadata::skipComment(){
  if(sr.nextCharIs('#')){
    sr.skipUntilLineBreak();
  }
}
