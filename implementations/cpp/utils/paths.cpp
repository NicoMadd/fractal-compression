#include "paths.hpp"

#include <cstdlib>
#include <filesystem>
#include <system_error>

#include "run_logging.hpp"

namespace file_paths {

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

}  // namespace file_paths
