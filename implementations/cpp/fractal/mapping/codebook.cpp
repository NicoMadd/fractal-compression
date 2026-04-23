#include "codebook.hpp"

#include <cstdlib>
#include <string>
#include <vector>

#include "../../utils/run_logging.hpp"
#include "fractal-mapping.hpp"

#include "../../files/writers/sequence-writer.hpp"
#include "../../files/writers/sequence-output.hpp"

using namespace std;


Codebook::Codebook(int rangeSize, int domainSize, vector<FractalMapping>* mappings) : rangeSize(rangeSize), domainSize(domainSize), mappings(*mappings) {}


void Codebook::save(string path) {
    SequenceWriter writer(path);
    if (!writer.ok()) {
        run_logging::error("could not open for write: " + path);
        std::exit(1);
    }
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
        so.space();
        so.write(mapping.transformation_type);
        so.bl();
    }
    

}
