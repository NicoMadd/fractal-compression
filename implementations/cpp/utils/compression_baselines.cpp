#include "compression_baselines.hpp"

#include <cstdint>
#include <exception>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <locale>
#include <sstream>
#include <string>
#include <vector>

#include "../image/metadata.hpp"
#include "../utils/paths.hpp"
#include "../utils/run_logging.hpp"

extern "C" {
#include "../third_party/miniz.h"
}
#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "../third_party/stb_image_write.h"

namespace compression_baselines {
namespace fs = std::filesystem;

static std::string formatCompressionRatio(std::uintmax_t numBytes, std::uintmax_t denBytes) {
  if (denBytes == 0) {
    return "n/a";
  }
  std::ostringstream oss;
  oss.imbue(std::locale::classic());
  oss << std::fixed << std::setprecision(2)
      << (static_cast<double>(numBytes) / static_cast<double>(denBytes)) << ":1";
  return oss.str();
}

static double ratioNumber(std::uintmax_t numBytes, std::uintmax_t denBytes) {
  if (denBytes == 0) {
    return 0.0;
  }
  return static_cast<double>(numBytes) / static_cast<double>(denBytes);
}

/** N in "N:1" minus M in "M:1" (same as subtracting the ratio values). */
static std::string formatRatioDiffToRef(double ref, double x, bool xValid) {
  if (ref == 0.0 || !xValid) {
    return "n/a";
  }
  const double d = x - ref;
  std::ostringstream oss;
  oss.imbue(std::locale::classic());
  oss << std::fixed << std::setprecision(2) << (d >= 0.0 ? "+" : "") << d;
  return oss.str();
}

static std::string fmt3(double value) {
  std::ostringstream oss;
  oss.imbue(std::locale::classic());
  oss << std::fixed << std::setprecision(3) << value;
  return oss.str();
}

int64_t writePngFromPgm(const std::string& pgmPath, const std::string& pngPath) {
  if (fs::path parent = fs::path(pngPath).parent_path(); !parent.empty()) {
    file_paths::ensureDirectoryExists(parent.string());
  }
  try {
    Matrix<GrayPixel> m = pgma::read_matrix(pgmPath);
    const int w = m.getCols();
    const int h = m.getRows();
    if (w <= 0 || h <= 0) {
      return 0;
    }
    std::vector<unsigned char> packed(static_cast<size_t>(w) * static_cast<size_t>(h));
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        packed[static_cast<size_t>(y) * static_cast<size_t>(w) + static_cast<size_t>(x)] =
            static_cast<unsigned char>(m.get(y, x).level);
      }
    }
    if (!stbi_write_png(pngPath.c_str(), w, h, 1, packed.data(), w)) {
      return 0;
    }
    return static_cast<int64_t>(fs::file_size(pngPath));
  } catch (...) {
    return 0;
  }
}

int64_t writeZipDeflatedFile(const std::string& filePath, const std::string& zipPath, const std::string& entryName) {
  if (fs::path parent = fs::path(zipPath).parent_path(); !parent.empty()) {
    file_paths::ensureDirectoryExists(parent.string());
  }
  std::ifstream in(filePath, std::ios::binary);
  if (!in) {
    return 0;
  }
  in.seekg(0, std::ios::end);
  const auto sz = in.tellg();
  in.seekg(0, std::ios::beg);
  if (sz <= 0) {
    return 0;
  }
  std::vector<unsigned char> buf(static_cast<size_t>(sz));
  in.read(reinterpret_cast<char*>(buf.data()), sz);
  if (in.gcount() != static_cast<std::streamsize>(buf.size())) {
    return 0;
  }
  {
    std::error_code ec;
    if (fs::exists(zipPath, ec) && !ec) {
      fs::remove(zipPath, ec);
    }
  }
  if (!mz_zip_add_mem_to_archive_file_in_place(
          zipPath.c_str(), entryName.c_str(), buf.data(), buf.size(), nullptr, 0,
          static_cast<mz_uint>(MZ_BEST_COMPRESSION))) {
    return 0;
  }
  try {
    return static_cast<int64_t>(fs::file_size(zipPath));
  } catch (...) {
    return 0;
  }
}

void printRunBaselinesAndCompressionRatios(const std::string& runDir, const std::string& codebookPath,
                                            const std::string& inputImagePath, int decompressionIterations, double mse,
                                            double mae, double psnr) {
  const fs::path bl = fs::path(runDir) / "baselines";
  const std::string baselines = bl.string();
  file_paths::ensureDirectoryExists(baselines);

  const fs::path originalCopy = fs::path(runDir) / "original.pgm";
  const std::string finalIterPgm = (fs::path(runDir) / "iterations" /
                                    ("iter_" + std::to_string(decompressionIterations) + ".pgm"))
                                       .string();

  const int64_t pngOrigB = writePngFromPgm(originalCopy.string(), (bl / "original.png").string());
  int64_t pngReconB = 0;
  {
    std::error_code ec;
    if (fs::exists(finalIterPgm, ec) && !ec) {
      pngReconB = writePngFromPgm(finalIterPgm, (bl / "reconstruction.png").string());
    }
  }

  const int64_t zipOrigB =
      writeZipDeflatedFile(originalCopy.string(), (bl / "original_deflated.zip").string(), "original.pgm");
  const std::string cbName = fs::path(codebookPath).filename().string();
  const int64_t zipCbB =
      writeZipDeflatedFile(codebookPath, (bl / "codebook_deflated.zip").string(), cbName);

  try {
    const std::uintmax_t bytesInput = fs::file_size(inputImagePath);
    std::uintmax_t bytesCodebook = 0;
    {
      std::error_code ec;
      if (fs::exists(codebookPath, ec) && !ec) {
        bytesCodebook = fs::file_size(codebookPath, ec);
      }
    }
    const std::uint64_t zipCb = zipCbB > 0
                                    ? static_cast<std::uint64_t>(zipCbB)
                                    : static_cast<std::uint64_t>(0);
    const std::string rawR = formatCompressionRatio(bytesInput, bytesCodebook);
    const std::string zipR =
        zipCb > 0 ? formatCompressionRatio(bytesInput, static_cast<std::uintmax_t>(zipCb)) : std::string("n/a");
    const std::string rPngS =
        pngOrigB > 0 ? formatCompressionRatio(bytesInput, static_cast<std::uintmax_t>(pngOrigB)) : std::string("n/a");
    const std::string rZorigS = zipOrigB > 0 ? formatCompressionRatio(bytesInput, static_cast<std::uintmax_t>(zipOrigB))
                                              : std::string("n/a");
    const std::uintmax_t codebookPlusZipB =
        bytesCodebook > 0 && zipCb > 0 ? bytesCodebook + static_cast<std::uintmax_t>(zipCb) : 0u;
    const std::string rCbPlusZipS = codebookPlusZipB > 0 ? formatCompressionRatio(bytesInput, codebookPlusZipB)
                                                          : std::string("n/a");

    const double rCb = ratioNumber(bytesInput, bytesCodebook);
    const double rPng = ratioNumber(bytesInput, pngOrigB > 0 ? static_cast<std::uintmax_t>(pngOrigB) : 0u);
    const double rZo = ratioNumber(bytesInput, zipOrigB > 0 ? static_cast<std::uintmax_t>(zipOrigB) : 0u);
    const double rZc = zipCb > 0 ? ratioNumber(bytesInput, static_cast<std::uintmax_t>(zipCb)) : 0.0;
    const double rCpsum = codebookPlusZipB > 0 ? ratioNumber(bytesInput, codebookPlusZipB) : 0.0;

    std::cout << "Compression ratio: " << rawR << " (PGM vs raw codebook), " << zipR
              << " (PGM vs zip codebook)\n";
    if (bytesCodebook > 0) {
      const bool vPng = pngOrigB > 0;
      const bool vZo = zipOrigB > 0;
      const bool vZc = zipCb > 0;
      const bool vCpsum = codebookPlusZipB > 0;
      std::cout << "  vs lossless PNG:  " << rPngS << "  (Δ vs raw codebook ratio: "
                << formatRatioDiffToRef(rCb, rPng, vPng) << ")\n";
      std::cout << "  vs DEFLATE of PGM: " << rZorigS << "  (Δ vs raw codebook ratio: "
                << formatRatioDiffToRef(rCb, rZo, vZo) << ")\n";
      std::cout << "  vs deflated .fc:  " << zipR << "  (Δ vs raw codebook ratio: " << formatRatioDiffToRef(rCb, rZc, vZc)
                << ")\n";
      std::cout << "  vs (raw .fc + deflated .fc zip) total: " << rCbPlusZipS
                << "  (Δ vs raw codebook ratio: " << formatRatioDiffToRef(rCb, rCpsum, vCpsum) << ")\n";
    }
    if (pngOrigB > 0) {
      std::cout << "PNG (8-bit lossless, same image as corresponding PGM): original " << pngOrigB << " B";
      if (pngReconB > 0) {
        std::cout << ", reconstruction " << pngReconB << " B";
      }
      std::cout << "\n";
    }
    std::cout << "MSE: " << fmt3(mse) << "  MAE: " << fmt3(mae) << "  PSNR: " << fmt3(psnr) << "\n";
    std::cout << "Baselines: " << fs::absolute(bl).string() << '\n';
  } catch (const std::exception& e) {
    run_logging::error(e.what());
  }
}

}  // namespace compression_baselines
