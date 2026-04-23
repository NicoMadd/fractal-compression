#include "error_metrics.hpp"

#include "metadata.hpp"

ImageErrorMetrics computeErrorMetricsFromPgmPaths(const std::string& refPgm, const std::string& otherPgm) {
    Matrix<GrayPixel> ref = pgma::read_matrix(refPgm);
    Matrix<GrayPixel> oth = pgma::read_matrix(otherPgm);
    return computeErrorVsReference(ref, oth);
}
