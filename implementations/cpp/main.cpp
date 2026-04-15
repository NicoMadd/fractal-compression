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

    ImageMetadata metadata = loadImage(params.imagePath);


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
