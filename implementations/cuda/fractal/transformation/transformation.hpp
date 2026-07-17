#pragma once

#include "../../utils/matrix.hpp"
#include "../../pixel/pixel.hpp"
#include "type.hpp"

#include <map>
#include <vector>

class Transformation {
    public:
        TransformationType type;
        Transformation(TransformationType type);
        virtual ~Transformation();
        virtual void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed)=0;
        static Transformation* from(TransformationType type);
    private:
        static std::map<TransformationType, Transformation*> transformations_;
};

namespace {
const std::vector<TransformationType> ALLOWED_TRANSFORMATIONS = {
    TransformationType::IDENTITY,
    TransformationType::ROTATION_CW_90,
    TransformationType::ROTATION_CW_180,
    TransformationType::ROTATION_CCW_90,
    TransformationType::REFLECTION_HZ,
    TransformationType::REFLECTION_VC,
    TransformationType::REFLECTION_FD,
    TransformationType::REFLECTION_SD,
};
}
class IdentityTransformation : public Transformation {
    public:
        IdentityTransformation() : Transformation(TransformationType::IDENTITY) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class RotationCW90Transformation : public Transformation {
    public:
        RotationCW90Transformation() : Transformation(TransformationType::ROTATION_CW_90) {};
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class RotationCW180Transformation : public Transformation {
    public:
        RotationCW180Transformation() : Transformation(TransformationType::ROTATION_CW_180) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class RotationCCW90Transformation : public Transformation {
    public:
        RotationCCW90Transformation() : Transformation(TransformationType::ROTATION_CCW_90) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class ReflectionHZTransformation : public Transformation {
    public:
        ReflectionHZTransformation() : Transformation(TransformationType::REFLECTION_HZ) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class ReflectionVCTransformation : public Transformation {
    public:
        ReflectionVCTransformation() : Transformation(TransformationType::REFLECTION_VC) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class ReflectionFDTransformation : public Transformation {
    public:
        ReflectionFDTransformation() : Transformation(TransformationType::REFLECTION_FD) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};

class ReflectionSDTransformation : public Transformation {
    public:
        ReflectionSDTransformation() : Transformation(TransformationType::REFLECTION_SD) {}
        void transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed);
};
