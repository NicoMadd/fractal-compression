#pragma once

#include <string>


class PipelineParams {
  public:
    std::string imagePath;
    int iterations = 0;
    int rangeSize = 4;
    int domainSize = 8;
    /** Compression thread pool size (Java `-p`). Default: `std::thread::hardware_concurrency()` or 1. */
    int compressionParallelism = 1;
    /** Decompression thread pool size (Java `-P`). Default: same as compression parallelism. */
    int decompressionParallelism = 1;
    /** When true, always re-run compression and overwrite the on-disk codebook (Java `-c`). */
    bool cleanCodebook = false;
    bool debug = false;

    /** Same rules as Java `PipelineParams.parse`: argv[0] is program name. */
    static std::optional<PipelineParams> parse(int argc, const char* argv[]);

    static void printUsage(const char* programPath);
};
