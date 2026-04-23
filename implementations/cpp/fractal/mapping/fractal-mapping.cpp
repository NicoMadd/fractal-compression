#include "fractal-mapping.hpp"

FractalMapping::FractalMapping() : range_x(0), range_y(0), domain_x(0), domain_y(0), s(0), o(0), transformation_type(TransformationType::IDENTITY) {}

FractalMapping::FractalMapping(int range_x, int range_y, int domain_x, int domain_y, float s, float o, TransformationType transformation_type)
    : range_x(range_x), range_y(range_y), domain_x(domain_x), domain_y(domain_y), s(s), o(o), transformation_type(transformation_type) {}
