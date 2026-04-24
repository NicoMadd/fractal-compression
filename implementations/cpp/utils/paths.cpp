#include "paths.hpp"

#include <cstdlib>
#include <filesystem>
#include <system_error>
#include <string>

#include "run_logging.hpp"

namespace file_paths {

std::string codebookPathForGeometry(const std::string& runDir, int rangeSize, int domainSize) {
  namespace fs = std::filesystem;
  return (fs::path(runDir) / "codebooks" /
          ("codebook_r" + std::to_string(rangeSize) + "_d" + std::to_string(domainSize) + ".fc"))
      .string();
}

std::string cppProcessRunDir(const std::string& imagePath) {
  namespace fs = std::filesystem;
  fs::path cwd = fs::current_path();
  fs::path repoRoot = cwd.parent_path().parent_path();
  fs::path stem = fs::path(imagePath).stem();
  return fs::absolute(repoRoot / "processes" / "cpp" / stem).string();
}

void ensureDirectoryExists(const std::string& dirPath) {
  std::error_code ec;
  std::filesystem::create_directories(dirPath, ec);
  if (ec) {
    run_logging::error("could not create directory " + dirPath + ": " + ec.message());
    std::exit(1);
  }
}

void cleanAndCreateIterationsDir(const std::string& runDir) {
  namespace fs = std::filesystem;
  const fs::path iter = fs::path(runDir) / "iterations";
  std::error_code ec;
  if (fs::exists(iter)) {
    fs::remove_all(iter, ec);
    if (ec) {
      run_logging::error("could not remove iterations directory " + iter.string() + ": " + ec.message());
      std::exit(1);
    }
  }
  ensureDirectoryExists(iter.string());
}

void copyOriginalToRunDir(const std::string& sourceImagePath, const std::string& runDir) {
  namespace fs = std::filesystem;
  const fs::path dest = fs::path(runDir) / "original.pgm";
  if (fs::path p = dest.parent_path(); !p.empty()) {
    ensureDirectoryExists(p.string());
  }
  std::error_code ec;
  fs::copy_file(sourceImagePath, dest, fs::copy_options::overwrite_existing, ec);
  if (ec) {
    run_logging::error("could not copy original to " + dest.string() + ": " + ec.message());
    std::exit(1);
  }
}

}  // namespace file_paths
