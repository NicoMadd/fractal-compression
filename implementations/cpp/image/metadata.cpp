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

static void readPgmHeader(SequenceReader& sr, int& width, int& height, int& maxVal) {
  vector<char> magic = sr.read(2);
  (void)magic;
  sr.readWhitespace();
  if (sr.nextCharIs('#')) {
    sr.skipUntilLineBreak();
  }
  width = sr.readNextInt();
  height = sr.readNextInt();
  maxVal = sr.readNextInt();
  sr.readWhitespace();
}

Matrix<GrayPixel> pgma::read_matrix(const string& path) {
  ifstream file(path);
  if (!file.is_open()) {
    run_logging::error("could not open image: " + path);
    std::exit(1);
  }
  SequenceReader sr(file);
  int width = 0;
  int height = 0;
  int maxVal = 0;
  readPgmHeader(sr, width, height, maxVal);
  (void)maxVal;
  Matrix<GrayPixel> out(height, width);
  for (int i = 0; i < height; i++) {
    for (int j = 0; j < width; j++) {
      int level = sr.readNextInt();
      out.set(i, j, GrayPixel(level));
    }
  }
  return out;
}
