#include <string>
#include "fractal/block/reduction/reduction-strategy.hpp"
#include "image/metadata.hpp"
#include "utils/utils.hpp"
#include "pipeline/pipeline.hpp"
#include "algorithms/gray-block-compression.hpp"

using namespace std;

struct PipelineParams {
    string imagePath;
};

PipelineParams validateParams(int argc, const char* argv[]){
    PipelineParams params = PipelineParams();
    params.imagePath = argv[1];
    return params;
}


int main(int argc, const char* argv[]){

    // read image path

    PipelineParams params = validateParams(argc, argv);

    print("Image path: " + params.imagePath);

    PGMAImageMetadata metadata = loadImage(params.imagePath);

    GrayBlockCompression gbc = GrayBlockCompression(4,8,1, MeanReductionStrategy());

    PGMAPipeline pipeline = PGMAPipeline(metadata, gbc);
    pipeline.run();

 

    // reduce blocks

    // match range - reduced domains

    // serialize file

    // DECOMPRESSION

    // initialize noise images

    // iterate n times

    // calculate errors
}
