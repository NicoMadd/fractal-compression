#include "gray-block-compression.hpp"

#include <cfloat>
#include <algorithm>
#include <atomic>
#include <cmath>
#include <iomanip>
#include <mutex>
#include <sstream>
#include <vector>
#include "../utils/run_logging.hpp"
#include "../fractal/block/block.hpp"
#include "../executors/executors.hpp"

using namespace std;

namespace {
std::atomic<long long> g_ls_zero_denominator_pairs{0};

string fmt_f(float v, int prec) {
    ostringstream oss;
    oss.imbue(locale::classic());
    oss << fixed << setprecision(prec) << v;
    return oss.str();
}
}  // namespace

GrayBlockCompression::GrayBlockCompression(int rbd, int dbd, int parallelism, ReductionStrategy* rs) : RBD(rbd), DBD(dbd), parallelism(parallelism), reductionStrategy(rs) {
}

int GrayBlockCompression::rangeSize() {
    return this->RBD;
}

int GrayBlockCompression::domainSize() {
    return this->DBD;
}

ReductionStrategy* GrayBlockCompression::getReductionStrategy(){
    return this->reductionStrategy;
}

vector<FractalMapping>* GrayBlockCompression::compress(PGMAImageMetadata metadata) {

    this->image_height = metadata.height;
    this->image_width = metadata.width;
    this->image_pixels = metadata.pixels;

    int range_cols = this->image_width / this->RBD;
    int range_rows = this->image_height / this->RBD;
    int domain_cols = this->image_width / this->DBD;
    int domain_rows = this->image_height / this->DBD;

    run_logging::debug("Compress: image " + to_string(this->image_width) + "x" + to_string(this->image_height)
            + ", range " + to_string(this->RBD) + ", domain " + to_string(this->DBD) + " -> "
            + to_string(range_cols) + "x" + to_string(range_rows) + " ranges, "
            + to_string(domain_cols) + "x" + to_string(domain_rows) + " domains");

    run_logging::debug("Compress: building range blocks...");
    this->build_ranges();
    run_logging::debug("Compress: building domain blocks...");
    this->build_domains();
    int domain_count = domain_cols * domain_rows;
    run_logging::debug("Compress: " + this->reductionStrategy->description() + " domain blocks ("
            + to_string(domain_count) + " blocks)...");
    this->build_reduced_domains();
    run_logging::debug("Compress: searching best domain + transform per range block...");
    vector<FractalMapping>* fractal_mappings = this->build_fractal_mappings();
    run_logging::debug("Compression Finished!");
    return fractal_mappings;
}


void GrayBlockCompression::build_domains(){

    int rows = this->image_height / this->DBD;
    int cols = this->image_width / this->DBD;


    this->domain_blocks = Matrix<Block>(rows, cols);

    for(int i=0;i<rows;i++){
        for(int j=0;j<cols;j++){
            this->domain_blocks.set(i, j, Block(this->image_pixels, i*this->DBD, j*this->DBD,this->DBD,this->DBD));
        }
    }
}


void GrayBlockCompression::build_ranges(){

    int rows = this->image_height / this->RBD;
    int cols = this->image_width / this->RBD;
    

    this->range_blocks = Matrix<Block>(rows, cols);
    for(int i=0;i<rows;i++){
        for(int j=0;j<cols;j++){
            this->range_blocks.set(i, j, Block(this->image_pixels, i*this->RBD, j*this->RBD,this->RBD,this->RBD));
        }
    }
}

void GrayBlockCompression::build_reduced_domains(){
    int rows = this->domain_blocks.getRows();
    int cols = this->domain_blocks.getCols();
    this->reduced_domain_blocks = Matrix<Block>(rows, cols);

    for(int i = 0; i < rows; ++i) {
        for(int j = 0; j < cols; ++j) {
            Block b = this->domain_blocks.get(i, j);
            Matrix<GrayPixel>* reduced_pixels = b.reduce(this->RBD, this->reductionStrategy);
            Block reduced_block(reduced_pixels, b.x, b.y, this->RBD, this->RBD);
            this->reduced_domain_blocks.set(i, j, reduced_block);
        }
    }

    if (run_logging::is_debug() && rows > 0 && cols > 0) {
        Block sample = this->reduced_domain_blocks.get(0, 0);
        run_logging::debug(
            "build_reduced_domains: grid " + to_string(rows) + "x" + to_string(cols)
            + " reduced blocks; sample [0,0] domain origin (" + to_string(sample.x) + "," + to_string(sample.y)
            + ") mean=" + fmt_f(sample.mean(), 4));
    }
}

void GrayBlockCompression::finalize_range_match(const RangeBlockMatchResult& r, const Block& range_block_for_debug,
                                                vector<FractalMapping>* fractal_mappings, int& done, int total_ranges,
                                                int step, float& sum_s, float& min_s_agg, float& max_s_agg) {
    fractal_mappings->push_back(r.mapping);
    sum_s += r.s_win;
    min_s_agg = min(min_s_agg, r.s_win);
    max_s_agg = max(max_s_agg, r.s_win);

    if (run_logging::is_debug() && done < 4 && r.win_di >= 0) {
        Block win_rd = this->reduced_domain_blocks.get(r.win_di, r.win_dj);
        const float mean_d_win = win_rd.mean();
        float ls_num = 0;
        float ls_den = 0;
        Block dbg = range_block_for_debug;
        calculate_s(dbg, win_rd, r.mean_r, mean_d_win, &ls_num, &ls_den, false);
        run_logging::debug("build_fractal_mappings: range #" + to_string(done) + " at ("
                           + to_string(range_block_for_debug.x) + "," + to_string(range_block_for_debug.y) + ") mean_r="
                           + fmt_f(r.mean_r, 4) + " -> best domain grid (" + to_string(r.win_di) + "," + to_string(r.win_dj)
                           + ") image origin (" + to_string(r.mapping.domain_x) + "," + to_string(r.mapping.domain_y)
                           + ") mean_d=" + fmt_f(win_rd.mean(), 4));
        run_logging::debug("  LS: numerator=" + fmt_f(ls_num, 6) + " denominator=" + fmt_f(ls_den, 6) + " mean_d="
                           + fmt_f(mean_d_win, 4) + " s=" + fmt_f(r.s_win, 6) + " o=" + fmt_f(r.o_win, 6)
                           + " mse_fit=" + fmt_f(r.min_error / static_cast<float>(RBD * RBD), 6));
    }

    done++;
    if (done == 1 || done == total_ranges || done % step == 0) {
        run_logging::debug("Compress: matched range blocks " + to_string(done) + "/" + to_string(total_ranges));
    }
}

vector<FractalMapping>* GrayBlockCompression::build_fractal_mappings(){


    vector<FractalMapping>* fractal_mappings = new vector<FractalMapping>();

    int total_ranges = this->range_blocks.getRows() * this->range_blocks.getCols();
    int step = max(1, total_ranges / 25);
    int done = 0;
    float sum_s = 0;
    float min_s_agg = FLT_MAX;
    float max_s_agg = -FLT_MAX;

    const int n_workers = max(1, this->parallelism);

    std::mutex merge_mtx;
    Executor executor(n_workers);
    for (vector<Block> range : this->range_blocks.getData()) {
        vector<Block> range_row = range;
        executor.submit([this, range_row, fractal_mappings, &merge_mtx, &done, total_ranges, step, &sum_s, &min_s_agg,
                         &max_s_agg]() {
            DomainFinder finder(this->reduced_domain_blocks, *this);
            for (Block range_block : range_row) {
                RangeBlockMatchResult r = finder.findBest(range_block);
                std::lock_guard<std::mutex> lock(merge_mtx);
                this->finalize_range_match(r, range_block, fractal_mappings, done, total_ranges, step, sum_s, min_s_agg,
                                           max_s_agg);
            }
        });
    }
    executor.shutdown();
    executor.join();

    if (run_logging::is_debug() && total_ranges > 0) {
        const float mean_s = sum_s / static_cast<float>(total_ranges);
        run_logging::debug(
            "build_fractal_mappings: s stats over all ranges — min=" + fmt_f(min_s_agg, 6) + " max=" + fmt_f(max_s_agg, 6)
            + " mean=" + fmt_f(mean_s, 6));
        run_logging::debug(
            "calculate_s: pairs with zero denominator (s forced to 0): "
            + to_string(g_ls_zero_denominator_pairs.load())
            + " (out of " + to_string(static_cast<long long>(total_ranges) * this->reduced_domain_blocks.getRows()
                                      * this->reduced_domain_blocks.getCols())
            + " range×domain evaluations)");
    }

    return fractal_mappings;
}

float GrayBlockCompression::calculate_s(Block& range, Block& reduced_domain,float mean_r, float mean_d,
                                         float* out_numerator, float* out_denominator,
                                         bool count_zero_denominator){

    float numerator=0;
    float denominator=0;

    for(int i=0; i<RBD;i++){
        for(int j=0; j<RBD;j++){
            float range_diff = range.get(i, j).level - mean_r;
            float domain_diff = reduced_domain.get(i, j).level - mean_d;

            numerator += range_diff * domain_diff;
            denominator += pow(domain_diff,2);
        }
    }

    if (out_numerator != nullptr) {
        *out_numerator = numerator;
    }
    if (out_denominator != nullptr) {
        *out_denominator = denominator;
    }

    float s = 0;

    if (denominator != 0) {
        s = numerator / denominator;
    } else if (count_zero_denominator) {
        g_ls_zero_denominator_pairs.fetch_add(1, std::memory_order_relaxed);
    }

    return s;
}

float GrayBlockCompression::calculate_o(float mean_r, float mean_d, float s){
    return mean_r - s * mean_d;
}



