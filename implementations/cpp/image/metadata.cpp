#include "metadata.hpp"

#include <fstream>
#include <vector>
#include "../utils/utils.hpp"
#include <iostream>

ImageMetadata loadImage(string imagePath) {

  ifstream file(imagePath);

  PGMAImageMetadata metadata = PGMAImageMetadata(file);

  print(metadata.height);
  print(metadata.width);
  print(metadata.maxVal);

  print("metadata read");

  return ImageMetadata();
}


PGMAImageMetadata::PGMAImageMetadata(ifstream &ifs) : sr(ifs){
  readMagicNumber();
  sr.readWhitespace();

  skipComment();

  this->width = sr.readNextInt();
  this->height = sr.readNextInt();
  this->maxVal = sr.readNextInt();

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
