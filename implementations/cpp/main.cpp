#include <exception>
#include <iostream>
#include <optional>
#include <string>
#include "fractal/block/reduction/reduction-strategy.hpp"
#include "image/metadata.hpp"
#include "pipeline/pipeline.hpp"
#include "pipeline/pipeline-params.hpp"
#include "algorithms/gray-block-compression.hpp"
#include "utils/run_logging.hpp"
#include "utils/paths.hpp"

using namespace std;

int main(int argc, const char* argv[]) {

    try {
        std::optional<PipelineParams> parsed = PipelineParams::parse(argc, argv);
        if (!parsed.has_value()) {
            return 1;
        }
        PipelineParams params = *parsed;
        run_logging::set_debug(params.debug);

        cout << "Image path: " << params.imagePath << '\n';

        PGMAImageMetadata metadata = loadImage(params.imagePath);
        cout << "Loaded PGM: " << metadata.width << "x" << metadata.height << '\n';

        string runDir = file_paths::cppProcessRunDir(params.imagePath);
        file_paths::ensureDirectoryExists(runDir);
        cout << "Process output directory: " << runDir << '\n';

        GrayBlockCompression gbc = GrayBlockCompression(params.rangeSize, params.domainSize,
                                                          params.compressionParallelism, new MeanReductionStrategy());

        PGMAPipeline pipeline = PGMAPipeline(metadata, gbc, runDir, params.iterations,
                                            params.decompressionParallelism);
        pipeline.run();
    } catch (const exception& e) {
        run_logging::error(e.what());
        return 1;
    }

    return 0;
}
