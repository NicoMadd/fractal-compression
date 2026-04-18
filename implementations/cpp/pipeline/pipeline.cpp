#include "pipeline.hpp"

#include <iomanip>
#include <iostream>
#include <string>
#include <vector>

#include "../utils/run_logging.hpp"
#include "../utils/time.hpp"
#include "../fractal/mapping/codebook.hpp"
#include "../fractal/mapping/fractal-mapping.hpp"

#include <filesystem>

namespace {
  double fmtMs(double seconds) {
      return seconds * 1000.0;
  }
}

PGMAPipeline::PGMAPipeline(PGMAImageMetadata& metadata, GrayBlockCompression& gbc, std::string runDir)
    : metadata(metadata), gbc(gbc), runDir(std::move(runDir)) {
}

void PGMAPipeline::run() {
  time_util::Stopwatch compression_sw;
  std::vector<FractalMapping> fractalMappings = this->compress();
  const double compression_seconds = compression_sw.elapsed_seconds();

  time_util::Stopwatch codebook_sw;
  Codebook codebook(gbc.rangeSize(), gbc.domainSize(), fractalMappings);
  const std::filesystem::path codebookPath = std::filesystem::path(runDir) / "codebook.fc";
  codebook.save(codebookPath.string());
  const double codebook_seconds = codebook_sw.elapsed_seconds();

  std::cout << std::fixed << std::setprecision(2);
  std::cout << "--- Summary ---\n";
  std::cout << "Compression: " << fmtMs(compression_seconds) << " ms\n";
  std::cout << "Codebook save: " << fmtMs(codebook_seconds) << " ms\n";
  std::cout << "Codebook: " << codebookPath.string() << '\n';
  std::cout << "Range mappings: " << fractalMappings.size() << '\n';

  this->decompress(fractalMappings);
}

std::vector<FractalMapping> PGMAPipeline::compress() {
  return this->gbc.compress(this->metadata);

}

void PGMAPipeline::decompress(std::vector<FractalMapping> fractalMappings) {
    constexpr int kDecompressionIterations = 10;

    run_logging::debug(
        "Decompression start: image " + std::to_string(metadata.width) + "x" +
        std::to_string(metadata.height) + ", " + std::to_string(fractalMappings.size()) +
        " mappings, " + std::to_string(kDecompressionIterations) + " iterations");

    time_util::Stopwatch decompress_sw;

    Matrix<GrayPixel> img;
    Matrix<GrayPixel> next;

    img.resize(metadata.height, metadata.width);
    next.resize(metadata.height, metadata.width);

    fill(img, []{return randomGrayPixel();});
    fill(next, []{return randomGrayPixel();});

    run_logging::debug("Decompression: initialized noise buffers");

    for (int iter = 0; iter < kDecompressionIterations; iter++) {
      run_logging::debug("Decompression iteration " + std::to_string(iter + 1) + "/" +
                         std::to_string(kDecompressionIterations));

      for (size_t mi = 0; mi < fractalMappings.size(); mi++) {

        FractalMapping mapping = fractalMappings[mi];

        Matrix<GrayPixel> domainPixels; 
        domainPixels.resize(gbc.domainSize(), gbc.domainSize());

        copySquare(&img, &domainPixels, mapping.domain_x, mapping.domain_y, gbc.domainSize());

        Matrix<GrayPixel> newRangePixels = Matrix<GrayPixel>(gbc.rangeSize(),gbc.rangeSize());

        ReductionStrategy* rs = gbc.getReductionStrategy();

        Matrix<GrayPixel>* reducedDomain = rs->reduce(&domainPixels, gbc.rangeSize());

        float s = mapping.s;
        float o = mapping.o;

        for(int i=0;i<reducedDomain->getRows();i++){
          for(int j=0;j<reducedDomain->getCols();j++){
            GrayPixel gp = reducedDomain->get(i, j);

            int newLevel = s * gp.level + o;

            GrayPixel newGrayPixel = GrayPixel(newLevel);

            next.set(mapping.range_x+i, mapping.range_y+j, newGrayPixel);
          }
        }
      }

      const std::filesystem::path nextPath = std::filesystem::path(runDir) / ("next_" + std::to_string(iter));

      pgma::save(next, nextPath.string());
      run_logging::debug("Decompression wrote intermediate " + nextPath.string() + ".pgm");
      swap(img, next);
    }

    const std::filesystem::path finalBase = std::filesystem::path(runDir) / "final";
    pgma::save(img, finalBase.string());
    run_logging::debug("Decompression wrote " + finalBase.string() + ".pgm");

    const double decompress_seconds = decompress_sw.elapsed_seconds();

    std::cout << std::fixed << std::setprecision(2);
    std::cout << "--- Decompression ---\n";
    std::cout << "Time: " << fmtMs(decompress_seconds) << " ms\n";
    std::cout << "Iterations: " << kDecompressionIterations << '\n';
    std::cout << "Range mappings: " << fractalMappings.size() << '\n';
    std::cout << "Output: " << (std::filesystem::path(runDir) / "final.pgm").string() << '\n';
}
