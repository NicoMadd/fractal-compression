#include "gray-block-compression.hpp"

#include <cfloat>
#include <iostream>
#include <vector>
#include "../utils/utils.hpp"
#include "../fractal/block/block.hpp"
#include "../fractal/block/compressed.hpp"

using namespace std;

GrayBlockCompression::GrayBlockCompression(int rbd, int dbd, int parallelism, ReductionStrategy rs) : RBD(rbd), DBD(dbd), parallelism(parallelism), reductionStrategy(rs) {
}

vector<FractalMapping> GrayBlockCompression::compress(PGMAImageMetadata metadata) {

    this->image_height = metadata.height;
    this->image_width = metadata.width;
    this->image_pixels = metadata.pixels;

    this->build_ranges();
    this->build_domains();
    this->build_reduced_domains();
    return this->build_fractal_mappings();
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
            Matrix<GrayPixel> reduced_pixels = b.reduce(this->RBD, this->reductionStrategy);
            Block reduced_block(reduced_pixels, b.x, b.y, this->RBD, this->RBD);
            this->reduced_domain_blocks.set(i, j, reduced_block);
        }
    }
}

vector<FractalMapping> GrayBlockCompression::build_fractal_mappings(){


    vector<FractalMapping> fractal_mappings;

    for(vector<Block> range : this->range_blocks.getData()){
        for(Block range_block : range){
            float mean_r = range_block.mean();


            float min_error = FLT_MAX;
            CompressedBlock* best_compressed_block = nullptr;

            for(int i = 0; i < this->reduced_domain_blocks.getRows(); i++){
                for(int j = 0; j < this->reduced_domain_blocks.getCols(); j++){
                    Block reduced_domain_block = this->reduced_domain_blocks.get(i, j);
                    float mean_d = reduced_domain_block.mean();

                    float s = calculate_s(range_block, reduced_domain_block,mean_r, mean_d);
                    float o = calculate_o(mean_r,mean_d, s);

                    float error = 0;

                    for(int k=0;k<reduced_domain_block.height;k++){
                        for(int l=0;l<reduced_domain_block.width;l++){
                            float approx_range = s * reduced_domain_block.get(k, l).level + o;
                            float range_diff = range_block.get(k, l).level - approx_range;
                            error += pow(range_diff,2);
                        }
                    }

                    if(error < min_error){
                        min_error = error;
                        if(best_compressed_block != nullptr){
                            delete best_compressed_block;
                        }
                        best_compressed_block = new CompressedBlock(&range_block, &reduced_domain_block, s, o);
                    }
                }
            }
            

            fractal_mappings.push_back(FractalMapping(range_block.x, range_block.y, best_compressed_block->domain->x, best_compressed_block->domain->y, best_compressed_block->s, best_compressed_block->o));
        }
    }

    return fractal_mappings;
}

float GrayBlockCompression::calculate_s(Block& range, Block& reduced_domain,float mean_r, float mean_d){

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

    float s = 0;

    if (denominator != 0) {
        s = numerator / denominator;
    }

    return s;
}

float GrayBlockCompression::calculate_o(float mean_r, float mean_d, float s){
    return mean_r - s * mean_d;
}



