#include "gray-block-compression.hpp"

#include "../executors/executors.hpp"
#include "../fractal/block/block.hpp"
#include "../utils/run_logging.hpp"
#include <algorithm>
#include <chrono>
#include <iomanip>
#include <iostream>
#include <mutex>
#include <sstream>
#include <vector>

using namespace std;

namespace {
string fmt_f(float v, int prec) {
  ostringstream oss;
  oss.imbue(locale::classic());
  oss << fixed << setprecision(prec) << v;
  return oss.str();
}
} // namespace

GrayBlockCompression::GrayBlockCompression(int rbd, int dbd, int parallelism,
                                           ReductionStrategy *rs)
    : RBD(rbd), DBD(dbd), parallelism(parallelism), reduction_strategy(rs) {}

int GrayBlockCompression::range_size() { return this->RBD; }

int GrayBlockCompression::domain_size() { return this->DBD; }

ReductionStrategy *GrayBlockCompression::get_reduction_strategy() {
  return this->reduction_strategy;
}

vector<FractalMapping> *
GrayBlockCompression::compress(PGMAImageMetadata metadata) {

  this->image_height = metadata.height;
  this->image_width = metadata.width;
  this->image_pixels = metadata.pixels;

  int range_rows = this->image_height / this->RBD;
  int range_cols = this->image_width / this->RBD;
  int domain_rows = this->image_height / this->DBD;
  int domain_cols = this->image_width / this->DBD;

  run_logging::debug(
      "Compress: image " + to_string(this->image_width) + "x" +
      to_string(this->image_height) + ", range " + to_string(this->RBD) +
      ", domain " + to_string(this->DBD) + " -> " + to_string(range_cols) +
      "x" + to_string(range_rows) + " ranges, " + to_string(domain_cols) + "x" +
      to_string(domain_rows) + " domains");

  run_logging::debug("Compress: building range blocks...");
  this->build_ranges();

  run_logging::debug("Compress: building domain blocks...");
  this->build_domains();

  int domain_count = domain_cols * domain_rows;
  run_logging::debug("Compress: " + this->reduction_strategy->description() +
                     " domain blocks (" + to_string(domain_count) +
                     " blocks)...");
  this->build_reduced_domains();

  run_logging::debug(
      "Compress: searching best domain + transform per range block...");
  vector<FractalMapping> *fractal_mappings = this->build_fractal_mappings();

  run_logging::debug("Compression Finished!");
  return fractal_mappings;
}

void GrayBlockCompression::build_domains() {

  int rows = this->image_height / this->DBD;
  int cols = this->image_width / this->DBD;

  const auto t0 = chrono::high_resolution_clock::now();

  this->domain_blocks = Matrix<Block>(rows, cols);
  for (int i = 0; i < rows; i++) {
    for (int j = 0; j < cols; j++) {
      this->domain_blocks.set(i, j,
                              Block(this->image_pixels, i * this->DBD,
                                    j * this->DBD, this->DBD, this->DBD));
    }
  }
  const auto t1 = chrono::high_resolution_clock::now();
  const auto duration =
      chrono::duration_cast<chrono::milliseconds>(t1 - t0).count();
  cout << "build_domains: " << duration << "ms\n";
}

void GrayBlockCompression::build_ranges() {

  int rows = this->image_height / this->RBD;
  int cols = this->image_width / this->RBD;

  const auto t0 = chrono::high_resolution_clock::now();

  this->range_blocks = Matrix<Block>(rows, cols);
  for (int i = 0; i < rows; i++) {
    for (int j = 0; j < cols; j++) {
      this->range_blocks.set(i, j,
                             Block(this->image_pixels, i * this->RBD,
                                   j * this->RBD, this->RBD, this->RBD));
    }
  }
  const auto t1 = chrono::high_resolution_clock::now();
  const auto duration =
      chrono::duration_cast<chrono::milliseconds>(t1 - t0).count();
  cout << "build_ranges: " << duration << "ms\n";
}

void GrayBlockCompression::build_reduced_domains() {
  int rows = this->domain_blocks.getRows();
  int cols = this->domain_blocks.getCols();
  this->reduced_domain_blocks = Matrix<Block>(rows, cols);

  const auto t0 = chrono::high_resolution_clock::now();
  for (int i = 0; i < rows; ++i) {
    for (int j = 0; j < cols; ++j) {
      Block b = this->domain_blocks.get(i, j);
      Matrix<GrayPixel> *reduced_pixels =
          b.reduce(this->RBD, this->reduction_strategy);
      Block reduced_block(reduced_pixels, b.x, b.y, this->RBD, this->RBD);
      this->reduced_domain_blocks.set(i, j, reduced_block);
    }
  }
  const auto t1 = chrono::high_resolution_clock::now();
  const auto duration =
      chrono::duration_cast<chrono::milliseconds>(t1 - t0).count();
  cout << "build_reduced_domains: " << duration << "ms\n";

  if (run_logging::is_debug() && rows > 0 && cols > 0) {
    Block sample = this->reduced_domain_blocks.get(0, 0);
    run_logging::debug("build_reduced_domains: grid " + to_string(rows) + "x" +
                       to_string(cols) +
                       " reduced blocks; sample [0,0] domain origin (" +
                       to_string(sample.x) + "," + to_string(sample.y) +
                       ") mean=" + fmt_f(sample.mean(), 4));
  }
}

vector<FractalMapping> *GrayBlockCompression::build_fractal_mappings() {

  vector<FractalMapping> *fractal_mappings = new vector<FractalMapping>();

  const int n_workers = max(1, this->parallelism);

  std::mutex merge_mtx;
  Executor executor(n_workers);
  const auto t0 = chrono::high_resolution_clock::now();
  for (vector<Block> range_row : this->range_blocks.getData()) {
    for (Block range_block : range_row) {
      executor.submit([this, range_block, fractal_mappings, &merge_mtx]() {
        DomainFinder finder(this->reduced_domain_blocks, *this);
        FractalMapping mapping = finder.findBest(range_block);
        {
          std::lock_guard<std::mutex> lock(merge_mtx);
          fractal_mappings->push_back(mapping);
        }
      });
    }
  }
  executor.shutdown();
  executor.join();

  const auto t1 = chrono::high_resolution_clock::now();
  const auto duration =
      chrono::duration_cast<chrono::milliseconds>(t1 - t0).count();
  cout << "build_fractal_mappings: " << duration << "ms\n";

  return fractal_mappings;
}
