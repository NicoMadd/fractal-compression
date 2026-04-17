#pragma once



class FractalMapping{
    
    public:
        int range_x;
        int range_y;
        int domain_x;
        int domain_y;
        float s;
        float o;
        FractalMapping(int range_x, int range_y, int domain_x, int domain_y, float s, float o);
        ~FractalMapping();
};
