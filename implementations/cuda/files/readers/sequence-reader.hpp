#pragma once

#include <fstream>
#include <string>
#include <vector>

using namespace std;

class SequenceReader {
public:
  explicit SequenceReader(std::ifstream &in);

  char read();
  vector<char> read(int n);
  char readWhitespace();
  int readInt();
  bool nextCharIs(char c);
  bool nextIsEof();
  void skipUntilLineBreak();
  int readNextInt();
  float readNextFloat();

private:
  ifstream *in_;
  vector<char> readUntilWhitespace();
  void skipFollowingWhitespaces();
};
