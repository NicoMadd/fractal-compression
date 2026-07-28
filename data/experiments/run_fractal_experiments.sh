#!/usr/bin/env bash
# Runs the fractal compression experiment described in
# docs/paper/materiales_y_metodos.tex: the four frozen 512x512 images under
# data/experiments/images/pgma/, each with the three block geometries (r=4,d=8;
# r=8,d=16; r=16,d=32), 20 decode iterations (0-indexed, so quality is available
# at iterations 5, 10 and 20), 12 compression and 12 decompression threads, and a
# freshly built codebook on every run.
#
# Invokes implementations/java/compression/run.sh with IMAGES_ROOT pointed at
# data/experiments/images so the frozen PGMA set is used (not data/images/pgma).
#
# Usage: data/experiments/run_fractal_experiments.sh [log-dir]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
MODULE_DIR="${REPO_ROOT}/implementations/java/compression"
IMAGES_ROOT="${SCRIPT_DIR}/images"
RESULTS_DIR="${SCRIPT_DIR}/results"
LOG_DIR="${1:-${REPO_ROOT}/processes/java/_experiment_logs}"

ITERATIONS=20
THREADS=12
GEOMETRIES=("4 8" "8 16" "16 32")
STEMS=(apollonian_gasket_512 lena baboon sun_tzu_512)

export BENCHMARK_GIT_SHA="${BENCHMARK_GIT_SHA:-$(git -C "${REPO_ROOT}" rev-parse HEAD 2>/dev/null || echo unknown)}"
export BENCHMARK_RUNNER_ID="${BENCHMARK_RUNNER_ID:-java-compression}"
export IMAGES_ROOT

mkdir -p "${LOG_DIR}"
cd "${MODULE_DIR}"

for stem in "${STEMS[@]}"; do
  if [ ! -f "${IMAGES_ROOT}/pgma/${stem}.ascii.pgm" ]; then
    echo "missing: ${IMAGES_ROOT}/pgma/${stem}.ascii.pgm" >&2
    exit 1
  fi
  for geometry in "${GEOMETRIES[@]}"; do
    read -r range domain <<<"${geometry}"
    log="${LOG_DIR}/${stem}_r${range}_d${domain}.log"
    echo "=== ${stem} r=${range} d=${domain} -> ${log}"
    ./run.sh pgma "${stem}" -i "${ITERATIONS}" -r "${range}" -d "${domain}" \
      -c -p "${THREADS}" -P "${THREADS}" 2>&1 | tee "${log}"

    run_dir="${REPO_ROOT}/processes/java/${stem}.ascii"
    archive="${RESULTS_DIR}/${stem}_r${range}_d${domain}"
    mkdir -p "${archive}"
    cp "${run_dir}/iterations/benchmark.csv" "${archive}/benchmark.csv"
    cp "${run_dir}/run_manifest.json" "${archive}/run_manifest.json"
  done
done

echo "done; registry: ${REPO_ROOT}/benchmarks/registry.jsonl"
