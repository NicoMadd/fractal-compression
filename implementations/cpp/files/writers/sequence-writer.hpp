#pragma once

#include "sequence-output.hpp"
#include <fstream>
#include <string>

using namespace std;

class SequenceWriter : public SequenceOutput {

    public:
        SequenceWriter(string path);
        ~SequenceWriter();
        void write(char c) override;
        void write(string text) override;
        void write(short number) override;
        void write(int number) override;
        void write(float number) override;
        void write(float number, int precision) override;
        void space() override;
        void bl() override;

    private:
        ofstream out;
};
