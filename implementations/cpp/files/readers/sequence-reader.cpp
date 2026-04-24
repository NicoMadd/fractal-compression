#include "./sequence-reader.hpp"
#include <cctype>
#include <vector>
#include <string>
#include <ios>
#include <stdexcept>

using namespace std;

bool isWhiteSpace(char c){
    return isspace(static_cast<unsigned char>(c));
}

SequenceReader::SequenceReader(std::ifstream &in) : in_(&in) {}

char SequenceReader::read(){
    char c;
    if (!in_->get(c)) {
      // failed or EOF
    }
    return c;
}

vector<char> SequenceReader::read(int n){
    vector<char> chars;

    for(int i=0;i<n;i++){
        chars.push_back(in_->get());
    }

    return chars;
}

char SequenceReader::readWhitespace(){

    if(nextIsEof() || !isWhiteSpace(in_->peek())){
        throw std::runtime_error("Expected whitespace but did not find it at the current position.");
    }
    return in_->get();
}

vector<char> SequenceReader::readUntilWhitespace(){
    vector<char> vector;

    do{
        if(!nextIsEof() && !isWhiteSpace(in_->peek())){
            vector.push_back(in_->get());
        }else{
            break;
        }
    } while(true);

    return vector;
}

int SequenceReader::readInt(){
    vector<char> intChars = readUntilWhitespace();
    string s(intChars.begin(),intChars.end());
    return stoi(s);
}

bool SequenceReader::nextCharIs(char c){
    return in_->peek()==c;

}

bool SequenceReader::nextIsEof(){
    return (in_->peek()) == char_traits<char>::eof();
}

void SequenceReader::skipUntilLineBreak(){
    while(!nextCharIs('\n') && !nextIsEof()){
        in_->get();
    }
    if(nextCharIs('\n')){
        in_->get();
    }
}

void SequenceReader::skipFollowingWhitespaces(){
    while(!nextIsEof() && isWhiteSpace(in_->peek())){
        in_->get();
    }
}

int SequenceReader::readNextInt(){
    skipFollowingWhitespaces();
    return readInt();
}

float SequenceReader::readNextFloat() {
    skipFollowingWhitespaces();
    vector<char> chars = readUntilWhitespace();
    string s(chars.begin(), chars.end());
    return stof(s);
}
