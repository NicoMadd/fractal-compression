#include "run_logging.hpp"

#include <iostream>

namespace run_logging {

namespace {
bool g_debug = false;
}

void set_debug(bool debug) {
    g_debug = debug;
}

bool is_debug() {
    return g_debug;
}

void debug(const std::string& message) {
    if (g_debug) {
        std::cout << message << '\n';
    }
}

}  // namespace run_logging
