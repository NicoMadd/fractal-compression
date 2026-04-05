#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMP_DIR="$(cd "$SCRIPT_DIR/../compression" && pwd)"
REPO_ROOT="$(cd "$COMP_DIR/../../.." && pwd)"

MATRIX="${1:-$SCRIPT_DIR/matrix.example.txt}"

if [[ ! -f "$MATRIX" ]]; then
  echo "Usage: $0 [path-to-matrix-file]" >&2
  echo "Default: $SCRIPT_DIR/matrix.example.txt" >&2
  exit 1
fi

export BENCHMARK_GIT_SHA="$(git -C "$REPO_ROOT" rev-parse HEAD 2>/dev/null || true)"

echo "Repo: $REPO_ROOT  BENCHMARK_GIT_SHA=${BENCHMARK_GIT_SHA:-<empty>}" >&2

while IFS= read -r line || [[ -n "$line" ]]; do
  [[ "$line" =~ ^[[:space:]]*# ]] && continue
  [[ -z "${line// /}" ]] && continue
  echo "---- run.sh $line ----" >&2
  (cd "$COMP_DIR" && ./run.sh $line)
done < "$MATRIX"
