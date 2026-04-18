#include "metadata.hpp"

#include <cstdlib>
#include <fstream>
#include "../utils/utils.hpp"
#include "../utils/run_logging.hpp"
#include "../files/writers/sequence-writer.hpp"
#include <string>

PGMAImageMetadata loadImage(string imagePath) {

  ifstream file(imagePath);
  if (!file.is_open()) {
    run_logging::error("could not open image: " + imagePath);
    std::exit(1);
  }

  PGMAImageMetadata metadata = PGMAImageMetadata(file);

  print(metadata.height);
  print(metadata.width);
  print(metadata.maxVal);

  return  metadata;
}


PGMAImageMetadata::PGMAImageMetadata(ifstream &ifs) {

  SequenceReader sr(ifs);
  readMagicNumber(sr);
  sr.readWhitespace();

  skipComment(sr);

  this->width = sr.readNextInt();
  this->height = sr.readNextInt();
  this->maxVal = sr.readNextInt();

  this->pixels = new Matrix<GrayPixel>(this->height, this->width);
  
  sr.readWhitespace();

  for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
          readPixel(sr, i,j);
      }
  }  
}

void PGMAImageMetadata::readPixel(SequenceReader &sr, int x, int y){
  int level = sr.readNextInt();

  GrayPixel gp = GrayPixel(level);
  this->pixels->set(x, y, gp);

}

void PGMAImageMetadata::readMagicNumber(SequenceReader &sr){
  vector<char> magicNumber = sr.read(2);

  // TODO validate equals
  print(magicNumber);
}

void PGMAImageMetadata::skipComment(SequenceReader &sr){
  if(sr.nextCharIs('#')){
    sr.skipUntilLineBreak();
  }
}

void PGMAImageMetadata::save(string path){



  SequenceWriter so(path+".pgm");
  if (!so.ok()) {
    run_logging::error("could not open for write: " + path + ".pgm");
    std::exit(1);
  }
  so.write("P2");
  so.bl();
  so.write(this->width);
  so.space();
  so.write(this->height);
  so.bl();
  so.write(this->maxVal);
  so.bl();

  for(int i=0;i<this->height;i++){
    for(int j=0;j<this->width;j++){
      so.write(this->pixels->get(i, j).level);
      so.space();  
    }
  }
}

void pgma::save(Matrix<GrayPixel> pixels, string path){
  // width = columns, height = rows (matches Java PGMAImageMetadata(cols, rows, ...))
  PGMAImageMetadata metadata(pixels.getCols(), pixels.getRows(), 255, &pixels);

  metadata.save(path);
}
