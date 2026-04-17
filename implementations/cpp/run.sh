#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# All .cpp files under this directory (recursive)
sources=()
while IFS= read -r f; do
  sources+=("$f")
done < <(find . -name '*.cpp' | LC_ALL=C sort)

g++ -std=c++17 -Werror -o main.exe "${sources[@]}"

./main.exe "$@"
