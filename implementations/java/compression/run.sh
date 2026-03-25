#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out implementations.java.compression.src.Main "$@"
