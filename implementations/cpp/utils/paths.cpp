#include "paths.hpp"

#include <filesystem>

namespace file_paths {

std::string cppProcessRunDir(const std::string& imagePath) {
  namespace fs = std::filesystem;
  fs::path cwd = fs::current_path();
  fs::path repoRoot = cwd.parent_path().parent_path();
  fs::path stem = fs::path(imagePath).stem();
  return fs::absolute(repoRoot / "processes" / "cpp" / stem).string();
}

void ensureDirectoryExists(const std::string& dirPath) {
  std::filesystem::create_directories(dirPath);
}

}  // namespace file_paths
