#pragma once


#include "block.hpp"
#include "../transformation/type.hpp"


class CompressedBlock{
    public:
        Block* range;
        Block* domain;
        float s;
        float o;
        TransformationType transformation_type;
        CompressedBlock();
        CompressedBlock(Block* range, Block* domain, float s, float o, TransformationType transformation_type);
};
