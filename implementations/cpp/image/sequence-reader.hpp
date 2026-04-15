#pragma once

#include <fstream>

using namespace std;

class SequenceReader {
public:
  explicit SequenceReader(std::ifstream &in);

  char read();
  vector<char> read(int n);
  char readWhitespace();
  int readInt();
  bool nextCharIs(char c);
  void skipUntilLineBreak();
  int readNextInt();

private:
  ifstream *in_;
  vector<char> readUntilWhitespace();
  void skipFollowingWhitespaces();
};
