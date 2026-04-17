#include "pipeline.hpp"

#include "../utils/utils.hpp"
#include "../image/metadata.hpp"
#include "../algorithms/gray-block-compression.hpp"

PGMAPipeline::PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc) : metadata(metadata), gbc(gbc) {
}

void PGMAPipeline::run() {
  vector<FractalMapping> fractalMappings = this->compress();


  // first mapping
  FractalMapping firstMapping = fractalMappings[0];

  print(firstMapping.range_x);
  print(firstMapping.range_y);
  print(firstMapping.domain_x);
  print(firstMapping.domain_y);
  print(firstMapping.s);
  print(firstMapping.o);

}

vector<FractalMapping> PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

void PGMAPipeline::decompress() {
    print("Decompressing...");
}
