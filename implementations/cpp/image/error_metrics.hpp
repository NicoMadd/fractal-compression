#pragma once

#include "../pixel/pixel.hpp"
#include "../utils/matrix.hpp"

#include <algorithm>
#include <cmath>
#include <limits>
#include <string>

struct ImageErrorMetrics {
    double mse = 0;
    double mae = 0;
};

/**
 * MSE/MAE over the overlapping rectangle (min rows × min cols), same as Java
 * `PGMAUtils` when both rasters are indexed with the top-left
 * `height × width` of the original. Extra pixels in the larger image are ignored; no
 * silent zeros on mismatch.
 */
inline ImageErrorMetrics computeErrorVsReference(Matrix<GrayPixel>& ref, Matrix<GrayPixel>& recon) {
    ImageErrorMetrics out;
    const int rows = std::min(ref.getRows(), recon.getRows());
    const int cols = std::min(ref.getCols(), recon.getCols());
    if (rows <= 0 || cols <= 0) {
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

/** MSE/MAE by loading two P2 PGMs (matches Java `PGMAUtils.calculateErrorMetrics`). */
ImageErrorMetrics computeErrorMetricsFromPgmPaths(const std::string& refPgm, const std::string& otherPgm);

/** PSNR for 8-bit gray; matches {@code ErrorUtils.calculatePSNR} in Java. */
inline double psnrFromMse(double mse) {
    if (mse <= 0.0) {
        return std::numeric_limits<double>::infinity();
    }
    return 10.0 * std::log10((255.0 * 255.0) / mse);
}
