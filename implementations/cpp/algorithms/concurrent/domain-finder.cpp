#include "domain-finder.hpp"

#include <cfloat>
#include <cmath>

#include "../gray-block-compression.hpp"
#include "../../fractal/block/compressed.hpp"

using namespace std;

DomainFinder::DomainFinder(Matrix<Block>& reduced_domain_blocks, GrayBlockCompression& codec)
    : reduced_domain_blocks_(reduced_domain_blocks), codec_(codec) {}

RangeBlockMatchResult DomainFinder::findBest(const Block& range_block_in) {
    Block range_block = range_block_in;
    RangeBlockMatchResult out;
    out.mean_r = range_block.mean();

    float min_error = FLT_MAX;
    CompressedBlock* best_compressed_block = nullptr;
    int win_di = -1;
    int win_dj = -1;

    for (int i = 0; i < reduced_domain_blocks_.getRows(); i++) {
        for (int j = 0; j < reduced_domain_blocks_.getCols(); j++) {
            Block reduced_domain_block = reduced_domain_blocks_.get(i, j);
            float mean_d = reduced_domain_block.mean();

            float s = codec_.calculate_s(range_block, reduced_domain_block, out.mean_r, mean_d);
            float o = codec_.calculate_o(out.mean_r, mean_d, s);

            float error = 0;

            for (int k = 0; k < reduced_domain_block.height; k++) {
                for (int l = 0; l < reduced_domain_block.width; l++) {
                    float approx_range = s * reduced_domain_block.get(k, l).level + o;
                    float range_diff = range_block.get(k, l).level - approx_range;
                    error += pow(range_diff, 2);
                }
            }

            if (error < min_error) {
                min_error = error;
                win_di = i;
                win_dj = j;
                if (best_compressed_block != nullptr) {
                    delete best_compressed_block;
                }
                Block* new_domain_block = new Block(reduced_domain_block);
                best_compressed_block = new CompressedBlock(&range_block, new_domain_block, s, o);
            }
        }
    }

    out.min_error = min_error;
    out.win_di = win_di;
    out.win_dj = win_dj;
    out.s_win = best_compressed_block->s;
    out.o_win = best_compressed_block->o;
    out.mapping = FractalMapping(range_block.x, range_block.y, best_compressed_block->domain->x,
                                 best_compressed_block->domain->y, best_compressed_block->s, best_compressed_block->o);
    delete best_compressed_block;
    return out;
}
