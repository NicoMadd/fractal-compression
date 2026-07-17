#pragma once

#include <fstream>
#include <string>
#include <vector>
#include "fractal-mapping.hpp"
#include "../../files/writers/sequence-output.hpp"
#include "../../files/readers/sequence-reader.hpp"

using namespace std;

class Codebook {

    public:
        Codebook();
        Codebook(int rangeSize, int domainSize, vector<FractalMapping>* mappings);

        int getRangeSize() const { return rangeSize; }
        int getDomainSize() const { return domainSize; }
        const vector<FractalMapping>& getMappings() const { return mappings; }

        void save(string path);
        void read(string path);

    private:
        int rangeSize;
        int domainSize;
        vector<FractalMapping> mappings;

        void serialize(SequenceOutput& out);
        void deserialize(SequenceReader& in);
};
