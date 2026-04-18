#include "sequence-writer.hpp"

#include <fstream>
#include <iomanip>
#include <sstream>

using namespace std;

SequenceWriter::SequenceWriter(string path) : out(ofstream(path)) {}

SequenceWriter::~SequenceWriter() {
    out.flush();
    out.close();
}

void SequenceWriter::write(char c) { out.put(c); }

void SequenceWriter::write(string text) { out << text; }

void SequenceWriter::write(short number) { out << number; }

void SequenceWriter::write(int number) { out << number; }

void SequenceWriter::write(float number) { write(number, 2); }

void SequenceWriter::write(float number, int precision) {
    ostringstream oss;
    oss.imbue(locale::classic());
    oss << fixed << setprecision(precision) << number;
    write(oss.str());
}

void SequenceWriter::space() { write(' '); }

void SequenceWriter::bl() { write('\n'); }
