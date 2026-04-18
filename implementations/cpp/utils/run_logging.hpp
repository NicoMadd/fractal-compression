#pragma once

#include <string>

namespace run_logging {

void set_debug(bool debug);
bool is_debug();
void debug(const std::string& message);
/** Always prints to stderr (not gated by debug). */
void error(const std::string& message);

}  // namespace run_logging
