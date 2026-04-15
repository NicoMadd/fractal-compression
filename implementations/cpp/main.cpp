#include <iostream>
#include <string>
#include "pixel/pixel.hpp"
#include "image/metadata.hpp"
#include "utils/utils.hpp"

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

    print(metadata.height);
    print(metadata.width);
    print(metadata.maxVal);
    print((int)metadata.pixels.size());
    print((int)metadata.pixels[0].size());

    // load image

    // COMPRESSION

    // domain blocks

    // range blocks

    // reduce blocks

    // match range - reduced domains

    // serialize file

    // DECOMPRESSION

    // initialize noise images

    // iterate n times

    // calculate errors
}
