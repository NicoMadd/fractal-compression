#pragma once

#include <string>

namespace file_paths {

/** Repo-root {@code processes/cpp/<image-stem>/} when cwd is {@code implementations/cpp}. */
std::string cppProcessRunDir(const std::string& imagePath);

void ensureDirectoryExists(const std::string& dirPath);

}  // namespace file_paths
