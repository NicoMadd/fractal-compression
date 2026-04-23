#pragma once

#include "../transformation/type.hpp"

class FractalMapping{
    
    public:
        int range_x;
        int range_y;
        int domain_x;
        int domain_y;
        float s;
        float o;
        TransformationType transformation_type;
        FractalMapping();
        FractalMapping(int range_x, int range_y, int domain_x, int domain_y, float s, float o, TransformationType transformation_type);
};
