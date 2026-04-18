#pragma once

#include "../pixel/pixel.hpp"
#include "../utils/matrix.hpp"

#include <cmath>
#include <limits>

struct ImageErrorMetrics {
    double mse = 0;
    double mae = 0;
};

/** Per-pixel MSE and MAE vs reference (same dimensions as {@code recon}). */
inline ImageErrorMetrics computeErrorVsReference(Matrix<GrayPixel>& ref, Matrix<GrayPixel>& recon) {
    ImageErrorMetrics out;
    const int rows = ref.getRows();
    const int cols = ref.getCols();
    if (rows != recon.getRows() || cols != recon.getCols()) {
        return out;
    }
    double total_sq = 0;
    double total_abs = 0;
    const double n = static_cast<double>(rows) * static_cast<double>(cols);
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            const double a = static_cast<double>(ref.get(i, j).level);
            const double b = static_cast<double>(recon.get(i, j).level);
            const double d = a - b;
            total_sq += d * d;
            total_abs += std::abs(d);
        }
    }
    out.mse = total_sq / n;
    out.mae = total_abs / n;
    return out;
}

/** PSNR for 8-bit gray; matches {@code ErrorUtils.calculatePSNR} in Java. */
inline double psnrFromMse(double mse) {
    if (mse <= 0.0) {
        return std::numeric_limits<double>::infinity();
    }
    return 10.0 * std::log10((255.0 * 255.0) / mse);
}
