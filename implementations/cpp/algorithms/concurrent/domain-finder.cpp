#include "domain-finder.hpp"

#include <atomic>
#include <cfloat>
#include <cmath>

#include "../gray-block-compression.hpp"
#include "../../fractal/transformation/transformation.hpp"

using namespace std;

namespace {
std::atomic<long long> g_ls_zero_denominator_pairs{0};
}

long long zero_denominator_pair_count() { return g_ls_zero_denominator_pairs.load(); }

DomainFinder::DomainFinder(Matrix<Block>& reduced_domain_blocks, GrayBlockCompression& codec)
    : reduced_domain_blocks_(reduced_domain_blocks), codec_(codec) {}

float DomainFinder::calculate_s(Block* range, Matrix<GrayPixel>* reduced_domain_pixels, float mean_r, float mean_d,
                              float* out_numerator, float* out_denominator,
                                 bool count_zero_denominator) {
    const int rbd = codec_.range_size();
    float numerator = 0;
    float denominator = 0;
    for (int i = 0; i < rbd; i++) {
        for (int j = 0; j < rbd; j++) {
            float range_diff = range->get(i, j).level - mean_r;
            float domain_diff = reduced_domain_pixels->get(i, j).level - mean_d;
            numerator += range_diff * domain_diff;
            denominator += domain_diff * domain_diff;
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

float DomainFinder::calculate_o(float mean_r, float mean_d, float s) { return mean_r - s * mean_d; }

/**
 * @brief Find the best compressed block for a range block.
 * 
 * @param range_block_in The range block to find the best compressed block for.
 * @return RangeBlockMatchResult The best compressed block for the range block.
 */
FractalMapping DomainFinder::findBest(const Block& range_block_in) {
    Block range_block = range_block_in;

    float mean_r = range_block.mean();
    const int rbd = codec_.range_size();

    Matrix<GrayPixel> scratch(rbd, rbd);

    float min_error = FLT_MAX;
    int best_domain_x = -1;
    int best_domain_y = -1;
    float best_s = 0;
    float best_o = 0;
    TransformationType best_transformation_type = TransformationType::IDENTITY;


    // Iterate over all reduced domain blocks.
    for (int i = 0; i < reduced_domain_blocks_.getRows(); i++) {
        for (int j = 0; j < reduced_domain_blocks_.getCols(); j++) {
            Block reduced_domain_block = reduced_domain_blocks_.get(i, j);
            float mean_d = reduced_domain_block.mean();

            // Iterate over all allowed transformations.
            for (TransformationType transformation_type : ALLOWED_TRANSFORMATIONS) {
                Transformation* transformation = Transformation::from(transformation_type);

                transformation->transform(reduced_domain_block.pixels, &scratch);

                float s = calculate_s(&range_block, &scratch, mean_r, mean_d);
                float o = calculate_o(mean_r, mean_d, s);

                float error = 0;

                for (int k = 0; k < rbd; k++) {
                    for (int l = 0; l < rbd; l++) {
                        float approx_range = s * scratch.get(k, l).level + o;
                        float range_diff = range_block.get(k, l).level - approx_range;
                        error += range_diff * range_diff;
                    }
                }

                if (error < min_error) {
                    min_error = error;
                    best_domain_x = i;
                    best_domain_y = j;
                    best_s = s;
                    best_o = o;
                    best_transformation_type = transformation_type;
                }
            }
        }
    }
    
    Block domain_block = reduced_domain_blocks_.get(best_domain_x, best_domain_y);
    FractalMapping mapping(range_block.x, range_block.y, domain_block.x, domain_block.y, best_s, best_o, best_transformation_type);
    return mapping;
}
