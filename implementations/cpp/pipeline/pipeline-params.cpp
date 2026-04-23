#include "pipeline-params.hpp"

#include <climits>
#include <cstddef>
#include <iostream>
#include <optional>
#include <string>
#include <thread>
#include <vector>

namespace {

bool parseNonNegativeInt(const char* label, const char* s, int& out) {
    try {
        std::size_t idx = 0;
        const std::string str(s);
        long v = std::stol(str, &idx, 10);
        if (idx != str.size()) {
            std::cerr << label << " must be an integer.\n";
            return false;
        }
        if (v < 0) {
            std::cerr << label << " must be non-negative.\n";
            return false;
        }
        if (v > static_cast<long>(INT_MAX)) {
            std::cerr << label << " is too large.\n";
            return false;
        }
        out = static_cast<int>(v);
        return true;
    } catch (...) {
        std::cerr << label << " must be an integer.\n";
        return false;
    }
}

bool parsePositiveInt(const char* label, const char* s, int& out) {
    if (!parseNonNegativeInt(label, s, out)) {
        return false;
    }
    if (out <= 0) {
        std::cerr << label << " must be a positive integer.\n";
        return false;
    }
    return true;
}

}  // namespace

void PipelineParams::printUsage(const char* programPath) {
    std::cerr << "Usage: " << programPath
              << " <image.pgm> <iterations> [<range size> [<domain size>]] [-r <range>] [-d "
                 "<domain>] [-p <threads>] [-P <threads>] [-c] [--debug]\n";
    std::cerr << "  Default range size is 4 if omitted. Domain defaults to 2× range if omitted.\n";
    std::cerr << "  -r / -d override positional range/domain when given.\n";
    std::cerr << "  -p: compression parallelism (positive integer); default = hardware concurrency or 1.\n";
    std::cerr << "  -P: decompression parallelism; default = same as -p.\n";
    std::cerr << "  -c: force codebook rebuild (ignore existing codebook for this range/domain).\n";
}

std::optional<PipelineParams> PipelineParams::parse(int argc, const char* argv[]) {
    int rangeFlag = 0;
    int domainFlag = 0;
    int compressionParFlag = 0;
    int decompressionParFlag = 0;
    bool cleanCodebook = false;
    bool debug = false;
    std::vector<std::string> positionals;

    for (int i = 1; i < argc;) {
        std::string a = argv[i];
        if (a == "-c") {
            cleanCodebook = true;
            i += 1;
        } else if (a == "-p") {
            if (i + 1 >= argc) {
                std::cerr << "Usage: -p requires a thread count (positive integer).\n";
                return std::nullopt;
            }
            if (!parsePositiveInt("Compression parallelism", argv[i + 1], compressionParFlag)) {
                return std::nullopt;
            }
            i += 2;
        } else if (a == "-P") {
            if (i + 1 >= argc) {
                std::cerr << "Usage: -P requires a thread count (positive integer) for decompression.\n";
                return std::nullopt;
            }
            if (!parsePositiveInt("Decompression parallelism", argv[i + 1], decompressionParFlag)) {
                return std::nullopt;
            }
            i += 2;
        } else if (a == "-r") {
            if (i + 1 >= argc) {
                std::cerr << "Usage: -r requires a range size (positive integer).\n";
                return std::nullopt;
            }
            if (!parsePositiveInt("Range size", argv[i + 1], rangeFlag)) {
                return std::nullopt;
            }
            i += 2;
        } else if (a == "-d") {
            if (i + 1 >= argc) {
                std::cerr << "Usage: -d requires a domain size (positive integer).\n";
                return std::nullopt;
            }
            if (!parsePositiveInt("Domain size", argv[i + 1], domainFlag)) {
                return std::nullopt;
            }
            i += 2;
        } else if (a == "--debug") {
            debug = true;
            i += 1;
        } else {
            positionals.push_back(a);
            i += 1;
        }
    }

    if (positionals.size() < 2) {
        printUsage(argv[0]);
        return std::nullopt;
    }

    PipelineParams params;
    params.cleanCodebook = cleanCodebook;
    params.debug = debug;
    params.imagePath = positionals[0];
    if (!parseNonNegativeInt("Iterations", positionals[1].c_str(), params.iterations)) {
        return std::nullopt;
    }

    int rangeSize;
    if (rangeFlag > 0) {
        rangeSize = rangeFlag;
    } else if (positionals.size() >= 3) {
        if (!parsePositiveInt("Range size", positionals[2].c_str(), rangeSize)) {
            return std::nullopt;
        }
    } else {
        rangeSize = 4;
    }

    int domainSize;
    if (domainFlag > 0) {
        domainSize = domainFlag;
    } else if (positionals.size() >= 4) {
        if (!parsePositiveInt("Domain size", positionals[3].c_str(), domainSize)) {
            return std::nullopt;
        }
    } else {
        domainSize = rangeSize * 2;
    }

    params.rangeSize = rangeSize;
    params.domainSize = domainSize;

    unsigned hc = std::thread::hardware_concurrency();
    int defaultCompressionPar = hc == 0 ? 1 : static_cast<int>(hc);
    params.compressionParallelism =
        compressionParFlag > 0 ? compressionParFlag : defaultCompressionPar;
    params.decompressionParallelism =
        decompressionParFlag > 0 ? decompressionParFlag : params.compressionParallelism;

    return params;
}
