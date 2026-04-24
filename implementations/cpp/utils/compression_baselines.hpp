#pragma once

#include <cstdint>
#include <string>

namespace compression_baselines {

/** P2 PGM → 8-bit gray PNG; returns file size, or 0 on failure. */
std::int64_t writePngFromPgm(const std::string& pgmPath, const std::string& pngPath);

/**
 * Single DEFLATE entry (max level, same idea as Java {@code ZipOutputStream} / level 9).
 * Returns .zip file size, or 0 on failure.
 */
std::int64_t writeZipDeflatedFile(const std::string& filePath, const std::string& zipPath, const std::string& entryName);

/**
 * {@code .../baselines/} PNGs + zips, then prints compression ratios to {@code std::cout} (raw + zip
 * codebook, line-up vs PNG / DEFLATE original / deflated .fc / raw+.fc zip, each with Δ vs raw codebook
 * ratio), MSE line, and baselines path.
 */
void printRunBaselinesAndCompressionRatios(const std::string& runDir, const std::string& codebookPath,
                                            const std::string& inputImagePath, int decompressionIterations,
                                            double mse, double mae, double psnr);

}  // namespace compression_baselines
