#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# Set FORCE_COMPILE=1, or pass --fc, to always recompile. Otherwise skip g++
# when main.exe exists and is newer than all .cpp sources.
FORCE_COMPILE="${FORCE_COMPILE:-0}"

pass_args=()
force_compile_arg=0
for arg in "$@"; do
  case "$arg" in
    --fc) force_compile_arg=1 ;;
    *) pass_args+=("$arg") ;;
  esac
done

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

if [[ "$should_compile" -eq 0 && -f main.exe ]]; then
  for f in "${sources[@]}"; do
    if [[ "$f" -nt main.exe ]]; then
      should_compile=1
      break
    fi
  done
else
  should_compile=1
fi

if [[ "$should_compile" -eq 1 ]]; then
  g++ -std=c++17 -Werror -O3 -o main.exe "${sources[@]}"
  echo "Compiled executable: ./main.exe"
else
  echo "Skipping compile (main.exe up to date). Set FORCE_COMPILE=1 or use --fc to rebuild."
fi

run_debug=0
for arg in "${pass_args[@]}"; do
  if [[ "$arg" == "--debug" ]]; then
    run_debug=1
    break
  fi
done
if [[ "$run_debug" -eq 1 ]]; then
  echo "Running ./main.exe (verbose: --debug)…"
else
  echo "Running ./main.exe…"
fi
if [[ ${#pass_args[@]} -gt 0 ]]; then
  ./main.exe "${pass_args[@]}"
else
  ./main.exe
fi

echo "Execution completed."
