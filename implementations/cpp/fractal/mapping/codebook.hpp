#pragma once

#include <fstream>
#include <string>
#include <vector>
#include "fractal-mapping.hpp"
#include "../../files/writers/sequence-output.hpp"

using namespace std;

class Codebook {

    public:
        Codebook(int rangeSize, int domainSize, vector<FractalMapping>* mappings);
    public:
        void save(string path);

    private:
        int rangeSize;
        int domainSize;
        vector<FractalMapping> mappings;

        void serialize(SequenceOutput& out);
};
