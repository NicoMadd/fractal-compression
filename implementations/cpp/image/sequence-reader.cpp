#include "./sequence-reader.hpp"
#include <cctype>
#include <vector>
#include <string>

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
    char c = in_->peek();
    if(!isWhiteSpace(c)){
        throw std::runtime_error("Expected whitespace but found non-whitespace character.");
    }
    return in_->get();
}

vector<char> SequenceReader::readUntilWhitespace(){
    vector<char> vector;

    do{

        char c = in_->peek();

        if(!isWhiteSpace(c)){
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
void SequenceReader::skipUntilLineBreak(){
    while(!nextCharIs('\n')){
        in_->get();
    }
    in_->get();
}

void SequenceReader::skipFollowingWhitespaces(){
    while(isWhiteSpace(in_->peek())){
        in_->get();
    }
}

int SequenceReader::readNextInt(){
    skipFollowingWhitespaces();
    return readInt();
}
