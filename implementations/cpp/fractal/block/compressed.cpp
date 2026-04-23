#include "compressed.hpp"

CompressedBlock::CompressedBlock() : range(nullptr), domain(nullptr), s(0), o(0), transformation_type(TransformationType::IDENTITY) {}

CompressedBlock::CompressedBlock(Block* range, Block* domain, float s, float o, TransformationType transformation_type)
    : range(range), domain(domain), s(s), o(o), transformation_type(transformation_type) {}
