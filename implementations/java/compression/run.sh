#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
IMAGES_ROOT="${REPO_ROOT}/data/images"

usage() {
  echo "Usage: $0 [<image type>] <image name> <iterations>"
  echo "  image type: subdirectory under data/images (default: pgma)"
  echo "  image name: stem or prefix; must match exactly one file in that folder"
  echo "Example: $0 baboon 25"
  echo "Example: $0 pgma baboon 25"
  exit 1
}

TYPE="pgma"
if [ "$#" -eq 2 ]; then
  NAME="$1"
  ITERS="$2"
elif [ "$#" -eq 3 ]; then
  TYPE="$1"
  NAME="$2"
  ITERS="$3"
else
  usage
fi

SEARCH_DIR="${IMAGES_ROOT}/${TYPE}"
if [ ! -d "$SEARCH_DIR" ]; then
  echo "No such image type folder: ${SEARCH_DIR}"
  exit 1
fi

shopt -s nullglob
matches=( "${SEARCH_DIR}/${NAME}"* )
shopt -u nullglob

if [ "${#matches[@]}" -eq 0 ]; then
  echo "No file matching '${NAME}*' under ${SEARCH_DIR}"
  exit 1
fi
if [ "${#matches[@]}" -gt 1 ]; then
  echo "Ambiguous name '${NAME}'; matches:" >&2
  printf '  %s\n' "${matches[@]}" >&2
  exit 1
fi

IMAGE_PATH="${matches[0]}"

mkdir -p out
tmp_list=".javac-sources.$$"
find src -name '*.java' | sort >"${tmp_list}"
javac -d out @"${tmp_list}"
rm -f "${tmp_list}"

exec java -cp out implementations.java.compression.src.Main "${IMAGE_PATH}" "${ITERS}"
