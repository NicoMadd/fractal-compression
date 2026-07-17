#pragma once

#include <string>

namespace file_paths {

/** Repo-root {@code processes/cpp/<image-stem>/} when cwd is {@code implementations/cpp}. */
std::string cppProcessRunDir(const std::string& imagePath);

/** {@code <runDir>/codebooks/codebook_r{r}_d{d}.fc} — same layout as Java {@code Codebook.pathForGeometry}. */
std::string codebookPathForGeometry(const std::string& runDir, int rangeSize, int domainSize);

void ensureDirectoryExists(const std::string& dirPath);

/** Remove {@code <runDir>/iterations} if present, then create an empty directory (Java {@code FileUtils.cleanAndCreateIterationsDir}). */
void cleanAndCreateIterationsDir(const std::string& runDir);

/** {@code <runDir>/original.pgm} as a copy of the input (Java {@code FileUtils.copyOriginal}). */
void copyOriginalToRunDir(const std::string& sourceImagePath, const std::string& runDir);

}  // namespace file_paths
