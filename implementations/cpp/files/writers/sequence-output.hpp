#pragma once

#include <string>

using namespace std;

class SequenceOutput {

    public:
        virtual void write(char c) = 0;
        virtual void write(string text) = 0;
        virtual void write(short number) = 0;
        virtual void write(int number) = 0;
        virtual void write(float number) = 0;
        virtual void write(float number, int precision) = 0;
        virtual void space() = 0;
        virtual void bl() = 0;
};
