#include "codebook.hpp"

#include <cstdlib>
#include <fstream>
#include <stdexcept>
#include <string>
#include <vector>

#include "../../files/readers/sequence-reader.hpp"
#include "../../utils/run_logging.hpp"
#include "fractal-mapping.hpp"

#include "../../files/writers/sequence-writer.hpp"
#include "../../files/writers/sequence-output.hpp"

using namespace std;

namespace {

constexpr int kTransformationTypeCount = 8;

}  // namespace

Codebook::Codebook() : rangeSize(0), domainSize(0), mappings() {}

Codebook::Codebook(int rangeSize, int domainSize, vector<FractalMapping>* mappings)
    : rangeSize(rangeSize), domainSize(domainSize), mappings(*mappings) {}


void Codebook::save(string path) {
    SequenceWriter writer(path);
    if (!writer.ok()) {
        run_logging::error("could not open for write: " + path);
        std::exit(1);
    }
    serialize(writer);
}

void Codebook::read(string path) {
    ifstream fin(path);
    if (!fin) {
        throw runtime_error("could not open codebook for read: " + path);
    }
    SequenceReader sr(fin);
    deserialize(sr);
}

void Codebook::deserialize(SequenceReader& in) {
    vector<char> mag = in.read(2);
    string magic(mag.begin(), mag.end());
    if (magic != "FC") {
        throw runtime_error("codebook magic must be FC, got: " + magic);
    }
    rangeSize = in.readNextInt();
    domainSize = in.readNextInt();
    const int n = in.readNextInt();
    if (n < 0) {
        throw runtime_error("codebook mapping count must be non-negative");
    }
    vector<FractalMapping> loaded;
    loaded.reserve(static_cast<size_t>(n));
    for (int i = 0; i < n; ++i) {
        const int rx = in.readNextInt();
        const int ry = in.readNextInt();
        const int dx = in.readNextInt();
        const int dy = in.readNextInt();
        const float s = in.readNextFloat();
        const float o = in.readNextFloat();
        const int tord = in.readNextInt();
        if (tord < 0 || tord >= kTransformationTypeCount) {
            throw runtime_error("invalid transformation ordinal in codebook");
        }
        const auto t = static_cast<TransformationType>(tord);
        loaded.emplace_back(rx, ry, dx, dy, s, o, t);
    }
    mappings = std::move(loaded);
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
        so.write(static_cast<int>(mapping.transformation_type));
        so.bl();
    }
    

}
