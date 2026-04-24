#pragma once

#include <chrono>

namespace time_util {

/** Elapsed wall time using std::chrono::steady_clock (monotonic). */
class Stopwatch {
  public:
    using clock = std::chrono::steady_clock;

    Stopwatch() : start_(clock::now()) {}

    void reset() { start_ = clock::now(); }

    /** Seconds since construction or the last reset(). */
    double elapsed_seconds() const {
      return std::chrono::duration<double>(clock::now() - start_).count();
    }

  private:
    clock::time_point start_;
};

}  // namespace time_util
