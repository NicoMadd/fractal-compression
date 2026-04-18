#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# All .cpp files under this directory (recursive)
sources=()
while IFS= read -r f; do
  sources+=("$f")
done < <(find . -name '*.cpp' | LC_ALL=C sort)

g++ -std=c++17 -Werror -O3 -o main.exe "${sources[@]}"

echo "Compiled executable: ./main.exe"

echo "Running executable with debug mode..."
./main.exe "$@"

echo "Execution completed."
