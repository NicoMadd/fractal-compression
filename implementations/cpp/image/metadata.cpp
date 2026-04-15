#include "metadata.hpp"

#include <fstream>
#include <vector>
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


PGMAImageMetadata::PGMAImageMetadata(ifstream &ifs) : sr(ifs){
  readMagicNumber();
  sr.readWhitespace();

  skipComment();

  this->width = sr.readNextInt();
  this->height = sr.readNextInt();
  this->maxVal = sr.readNextInt();

  sr.readWhitespace();




  for (int i = 0; i < height; i++) {
      vector<GrayPixel> row;
      for (int j = 0; j < width; j++) {
          readPixel(row);
      }
      pixels.push_back(row);
  }

}

void PGMAImageMetadata::readPixel(vector<GrayPixel>& bucket){
  int level = sr.readNextInt();

  GrayPixel gp = GrayPixel(level);
  bucket.push_back(gp);

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
