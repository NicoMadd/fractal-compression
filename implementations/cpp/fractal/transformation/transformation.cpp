#include "transformation.hpp"

#include <map>
#include <stdexcept>

std::map<TransformationType, Transformation*> Transformation::transformations_ = {
    {TransformationType::IDENTITY, new IdentityTransformation()},
    {TransformationType::ROTATION_CW_90, new RotationCW90Transformation()},
    {TransformationType::ROTATION_CW_180, new RotationCW180Transformation()},
    {TransformationType::ROTATION_CCW_90, new RotationCCW90Transformation()},
    {TransformationType::REFLECTION_HZ, new ReflectionHZTransformation()},
    {TransformationType::REFLECTION_VC, new ReflectionVCTransformation()},
    {TransformationType::REFLECTION_FD, new ReflectionFDTransformation()},
    {TransformationType::REFLECTION_SD, new ReflectionSDTransformation()},
};

Transformation::Transformation(TransformationType type) : type(type) {}

Transformation::~Transformation() {}

Transformation* Transformation::from(TransformationType type) {
    const auto it = transformations_.find(type);
    if (it == transformations_.end()) {
        throw std::invalid_argument("Invalid transformation type");
    }
    return it->second;
}


void IdentityTransformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    copySquare(src, transformed, 0, 0, src->getRows());
}

void RotationCW90Transformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = j;
            int newY = rows - 1 - i;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}

void RotationCW180Transformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = rows - 1 - i;
            int newY = cols - 1 - j;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}

void RotationCCW90Transformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = cols - 1 - j;
            int newY = i;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}

void ReflectionHZTransformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = i;
            int newY = cols - 1 - j;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}

void ReflectionVCTransformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = rows - 1 - i;
            int newY = j;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}

void ReflectionFDTransformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = j;
            int newY = i;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}

void ReflectionSDTransformation::transform(Matrix<GrayPixel>* src, Matrix<GrayPixel>* transformed) {
    int rows = src->getRows();
    int cols = src->getCols();
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int newX = rows - 1 - j;
            int newY = cols - 1 - i;
            transformed->set(newX, newY, src->get(i, j));
        }
    }
}
