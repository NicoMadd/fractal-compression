#include "compressed.hpp"

CompressedBlock::CompressedBlock() : range(nullptr), domain(nullptr), s(0), o(0) {}

CompressedBlock::CompressedBlock(Block* range, Block* domain, float s, float o)
    : range(range), domain(domain), s(s), o(o) {}
