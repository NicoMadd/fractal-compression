#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
IMAGES_ROOT="${REPO_ROOT}/data/images"

usage() {
  echo "Usage: $0 [[<image type>] <image name>] [-i iterations] [-r range] [-d domain] [-p threads] [-c] [--no-iter-save] [--mr|--br] [-- <java-args>...]"
  echo "  image type: subdirectory under data/images (default: pgma)"
  echo "  image name: stem or prefix; must match exactly one file under the type folder"
  echo "  Defaults: iterations=25, range=4, domain=8; thread pool size defaults to JVM availableProcessors if -p omitted"
  echo "  -i, -r, -d, -p override defaults (any order before --). -c forces codebook rebuild (see codebook_r{r}_d{d}.fc)."
  echo "  --no-iter-save skips per-iteration PGM writes; errors printed once from final frame (faster)."
  echo "  --mr mean domain reduction (default); --br bicubic domain reduction. Not both."
  echo "  After --, remaining args are passed to Main as well."
  echo "Example: $0 baboon"
  echo "Example: $0 baboon -i 40"
  echo "Example: $0 baboon -c"
  echo "Example: $0 pgma baboon -r 8 -d 16"
  exit 1
}

JAVA_OPTS=()
PRE_JAVA=()
seen_sep=0
for arg in "$@"; do
  if [[ "$seen_sep" -eq 1 ]]; then
    JAVA_OPTS+=("$arg")
    continue
  fi
  if [[ "$arg" == "--" ]]; then
    seen_sep=1
    continue
  fi
  PRE_JAVA+=("$arg")
done

ITERS=25
RANGE=4
DOMAIN=8
PARALLELISM=""
CLEAN_CODEBOOK=0
NO_ITER_SAVE=0
REDUCTION_FLAG=""
TYPE="pgma"
POSITIONAL=()

i=0
n=${#PRE_JAVA[@]}
while [ "$i" -lt "$n" ]; do
  a="${PRE_JAVA[$i]}"
  case "$a" in
    -i)
      i=$((i + 1))
      if [ "$i" -ge "$n" ]; then echo "$0: -i requires a value"; exit 1; fi
      ITERS="${PRE_JAVA[$i]}"
      ;;
    -r)
      i=$((i + 1))
      if [ "$i" -ge "$n" ]; then echo "$0: -r requires a value"; exit 1; fi
      RANGE="${PRE_JAVA[$i]}"
      ;;
    -d)
      i=$((i + 1))
      if [ "$i" -ge "$n" ]; then echo "$0: -d requires a value"; exit 1; fi
      DOMAIN="${PRE_JAVA[$i]}"
      ;;
    -p)
      i=$((i + 1))
      if [ "$i" -ge "$n" ]; then echo "$0: -p requires a value"; exit 1; fi
      PARALLELISM="${PRE_JAVA[$i]}"
      ;;
    -c)
      CLEAN_CODEBOOK=1
      ;;
    --no-iter-save)
      NO_ITER_SAVE=1
      ;;
    --mr)
      if [ -n "$REDUCTION_FLAG" ] && [ "$REDUCTION_FLAG" != "--mr" ]; then echo "$0: use only one of --mr or --br"; exit 1; fi
      REDUCTION_FLAG="--mr"
      ;;
    --br)
      if [ -n "$REDUCTION_FLAG" ] && [ "$REDUCTION_FLAG" != "--br" ]; then echo "$0: use only one of --mr or --br"; exit 1; fi
      REDUCTION_FLAG="--br"
      ;;
    *)
      POSITIONAL+=("$a")
      ;;
  esac
  i=$((i + 1))
done

if [ "${#POSITIONAL[@]}" -eq 1 ]; then
  NAME="${POSITIONAL[0]}"
elif [ "${#POSITIONAL[@]}" -eq 2 ]; then
  TYPE="${POSITIONAL[0]}"
  NAME="${POSITIONAL[1]}"
else
  usage
fi

if ! [[ "$ITERS" =~ ^[0-9]+$ ]] || [ "$ITERS" -lt 0 ]; then
  echo "$0: iterations must be a non-negative integer"
  exit 1
fi
if ! [[ "$RANGE" =~ ^[0-9]+$ ]] || [ "$RANGE" -le 0 ]; then
  echo "$0: range size must be a positive integer"
  exit 1
fi
if ! [[ "$DOMAIN" =~ ^[0-9]+$ ]] || [ "$DOMAIN" -le 0 ]; then
  echo "$0: domain size must be a positive integer"
  exit 1
fi
if [ -n "$PARALLELISM" ]; then
  if ! [[ "$PARALLELISM" =~ ^[0-9]+$ ]] || [ "$PARALLELISM" -le 0 ]; then
    echo "$0: -p must be a positive integer"
    exit 1
  fi
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

MAIN_ARGS=("${IMAGE_PATH}" "${ITERS}" "${RANGE}" "${DOMAIN}")
if [ "$CLEAN_CODEBOOK" -eq 1 ]; then
  MAIN_ARGS+=(-c)
fi
if [ "$NO_ITER_SAVE" -eq 1 ]; then
  MAIN_ARGS+=(--no-iter-save)
fi
if [ -n "$REDUCTION_FLAG" ]; then
  MAIN_ARGS+=("$REDUCTION_FLAG")
fi
if [ -n "$PARALLELISM" ]; then
  MAIN_ARGS+=(-p "$PARALLELISM")
fi
if [ "${#JAVA_OPTS[@]}" -gt 0 ]; then
  MAIN_ARGS+=("${JAVA_OPTS[@]}")
fi
exec java -cp out implementations.java.compression.src.Main "${MAIN_ARGS[@]}"
