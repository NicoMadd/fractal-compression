#include <cstdlib>
#include <exception>
#include <iostream>
#include <string>
#include "fractal/block/reduction/reduction-strategy.hpp"
#include "image/metadata.hpp"
#include "pipeline/pipeline.hpp"
#include "algorithms/gray-block-compression.hpp"
#include "utils/run_logging.hpp"
#include "utils/paths.hpp"

using namespace std;

struct PipelineParams {
    string imagePath;
    bool debug = false;
};

PipelineParams validateParams(int argc, const char* argv[]){
    PipelineParams params = PipelineParams();
    if (argc < 2) {
        cerr << "Usage: " << argv[0] << " <image.pgm> [--debug]\n";
        exit(1);
    }
    for (int i = 1; i < argc; ++i) {
        string a = argv[i];
        if (a == "--debug") {
            params.debug = true;
        } else if (params.imagePath.empty()) {
            params.imagePath = a;
        }
    }
    if (params.imagePath.empty()) {
        cerr << "Usage: " << argv[0] << " <image.pgm> [--debug]\n";
        exit(1);
    }
    return params;
}


int main(int argc, const char* argv[]){

    try {
        PipelineParams params = validateParams(argc, argv);
        run_logging::set_debug(params.debug);

        cout << "Image path: " << params.imagePath << '\n';

        PGMAImageMetadata metadata = loadImage(params.imagePath);
        cout << "Loaded PGM: " << metadata.width << "x" << metadata.height << '\n';

        string runDir = file_paths::cppProcessRunDir(params.imagePath);
        file_paths::ensureDirectoryExists(runDir);
        cout << "Process output directory: " << runDir << '\n';

        GrayBlockCompression gbc = GrayBlockCompression(8,16,1, new MeanReductionStrategy());

        PGMAPipeline pipeline = PGMAPipeline(metadata, gbc, runDir);
        pipeline.run();
    } catch (const std::exception& e) {
        run_logging::error(e.what());
        return 1;
    }

 

    // reduce blocks

    // match range - reduced domains

    // serialize file

    // DECOMPRESSION

    // initialize noise images

    // iterate n times

    // calculate errors

    return 0;
}
