#pragma once

#include <string>


class PipelineParams {
  public:
    std::string imagePath;
    int iterations = 0;
    int rangeSize = 4;
    int domainSize = 8;
    bool debug = false;

    /** Same rules as Java `PipelineParams.parse`: argv[0] is program name. */
    static std::optional<PipelineParams> parse(int argc, const char* argv[]);

    static void printUsage(const char* programPath);
};
