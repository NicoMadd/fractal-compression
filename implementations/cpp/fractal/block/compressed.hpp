#pragma once


#include "block.hpp"


class CompressedBlock{
    public:
        Block* range;
        Block* domain;
        float s;
        float o;
        CompressedBlock();
        CompressedBlock(Block* range, Block* domain, float s, float o);
};
