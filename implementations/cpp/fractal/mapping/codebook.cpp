#include "codebook.hpp"

#include <fstream>
#include <ios>
#include <string>
#include <vector>
#include "fractal-mapping.hpp"

#include "../../files/writers/sequence-writer.hpp"
#include "../../files/writers/sequence-output.hpp"

using namespace std;


Codebook::Codebook(int rangeSize, int domainSize, vector<FractalMapping> mappings) : rangeSize(rangeSize), domainSize(domainSize), mappings(mappings) {}


void Codebook::save(string path) {
    SequenceWriter writer(path);
    serialize(writer);
}

void Codebook::serialize(SequenceOutput& so){

    so.write("FC");
    so.space();
    so.write(rangeSize);
    so.space();
    so.write(domainSize);
    so.space();
    so.write((int)mappings.size());
    so.bl();

    for (FractalMapping mapping : mappings) {
        so.write(mapping.range_x);
        so.space();
        so.write(mapping.range_y);
        so.space();
        so.write(mapping.domain_x);
        so.space();
        so.write(mapping.domain_y);
        so.space();
        so.write(mapping.s);
        so.space();
        so.write(mapping.o);
        so.bl();
    }
    



}
