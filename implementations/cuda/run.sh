#!/usr/bin/env bash
# No `set -u` so an empty `pass_args` is safe in all bash versions
set -eo pipefail

cd "$(dirname "$0")"

# Set FORCE_COMPILE=1, or pass --fc, to always recompile. Otherwise skip g++
# when main.exe exists and is newer than all .cpp sources.
FORCE_COMPILE="${FORCE_COMPILE:-0}"

# Set flags used by both Mac and Linux
COMMON_FLAGS=(-std=c++17 -Werror -Wall -Wextra -Wno-deprecated-declarations)

# Handle platform-specific quirks
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac (Clang / libc++)
    PLATFORM_FLAGS=(-D_LIBCPP_REMOVE_TRANSITIVE_INCLUDES)
else
    # Linux (GCC / libstdc++)
    PLATFORM_FLAGS=(-D_GLIBCXX_RELEASE) # Strict include hygiene for modern GCC
fi

pass_args=()
force_compile_arg=0
for arg in "$@"; do
  case "$arg" in
    --fc) force_compile_arg=1 ;;
    --release) release_arg=1 ;;
    *) pass_args+=("$arg") ;;
  esac
done

# Development flags or release flags
if [[ "$release_arg" -eq 1 ]]; then
  COMMON_FLAGS+=(-O3)
  echo "Using release flags"
else
  COMMON_FLAGS+=(-O0 -g)
  echo "Using development flags"
fi

# All .cpp files under this directory (recursive)
sources=()
while IFS= read -r f; do
  sources+=("$f")
done < <(find . -name '*.cpp' | LC_ALL=C sort)

should_compile=0
case "${FORCE_COMPILE}" in
  1|true|yes|on) should_compile=1 ;;
esac
[[ "$force_compile_arg" -eq 1 ]] && should_compile=1

# Miniz (C) for DEFLATE .zip baselines, same as Java {@code ZipOutputStream} / level 9
miniz_c=(third_party/miniz.c third_party/miniz_tdef.c third_party/miniz_tinfl.c third_party/miniz_zip.c)
miniz_o=()
for c in "${miniz_c[@]}"; do
  miniz_o+=("${c%.c}.o")
done

if [[ "$should_compile" -eq 0 && -f main ]]; then
  for f in "${sources[@]}"; do
    if [[ "$f" -nt main ]]; then
      should_compile=1
      break
    fi
  done
  for c in "${miniz_c[@]}"; do
    o="${c%.c}.o"
    if [[ ! -f "$o" || "$c" -nt "$o" || "$c" -nt main ]]; then
      should_compile=1
      break
    fi
  done
else
  should_compile=1
fi

if [[ "$should_compile" -eq 1 ]]; then
  for c in "${miniz_c[@]}"; do
    o="${c%.c}.o"
    # Third-party C; -Werror would fail on all warnings in miniz
    gcc -O3 -std=c11 -c -I"$(pwd)/third_party" -o "$o" "$c"
  done
  # stb_image_write.h uses sprintf in HDR path (not used for our PNGs); third-party
  g++ "${COMMON_FLAGS[@]}" "${PLATFORM_FLAGS[@]}" -o main "${sources[@]}" "${miniz_o[@]}"
  echo "Compiled executable: ./main"
else
  echo "Skipping compile (main executable up to date). Set FORCE_COMPILE=1 or use --fc to rebuild."
fi

run_debug=0
for arg in "${pass_args[@]}"; do
  if [[ "$arg" == "--debug" ]]; then
    run_debug=1
    break
  fi
done
if [[ "$run_debug" -eq 1 ]]; then
  echo "Running ./main (verbose: --debug)…"
else
  echo "Running ./main…"
fi
if [[ ${#pass_args[@]} -gt 0 ]]; then
  ./main "${pass_args[@]}"
else
  ./main
fi

echo "Execution completed."
