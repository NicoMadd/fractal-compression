#!/usr/bin/env bash
# Run PGMA benchmark matrices: compression then decompression (see matrix.all-pgma.txt).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

"$SCRIPT_DIR/run_matrix.sh" "$SCRIPT_DIR/matrix.compression-pgma.txt"
"$SCRIPT_DIR/run_matrix.sh" "$SCRIPT_DIR/matrix.decompression-pgma.txt"
