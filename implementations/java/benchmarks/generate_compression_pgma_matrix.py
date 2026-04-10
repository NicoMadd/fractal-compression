#!/usr/bin/env python3
"""
Regenerate matrix.compression-pgma.txt from the parameters below.

Every run uses -c (full encode), --no-iter-save. Paired -p / -P (same pool sizes).

From repo root:
  python3 implementations/java/benchmarks/generate_compression_pgma_matrix.py
"""

from __future__ import annotations

import os

# --- parameters (edit here) ---
STEMS = [
    "lena.ascii",
]

# Explicit (range, domain) pairs (512×512 PGMA-friendly).
GEOMETRIES = [
    (4, 8),
    (8, 16),
    (16, 32),
    (32, 64),
    (4, 16),
    (4, 32),
    (4, 128),
    (8, 32),
    (8, 64),
    (8, 128),
]

ITERATIONS = [10]

# Compression/decode thread pools (-p / -P), paired equally.
PARALLEL_PAIRS = [(1, 1), (2, 2), (4, 4), (8, 8), (16, 16)]

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUT_FILE = os.path.join(SCRIPT_DIR, "matrix.compression-pgma.txt")


def main() -> None:
    lines: list[str] = []
    n = len(STEMS) * len(GEOMETRIES) * len(ITERATIONS) * len(PARALLEL_PAIRS)

    lines.append("# PGMA matrix — compression (encode / codebook build)")
    lines.append("# Image: lena.ascii | REGENERATE: python3 implementations/java/benchmarks/generate_compression_pgma_matrix.py")
    lines.append("# From repo root:")
    lines.append("#   ./implementations/java/benchmarks/run_matrix.sh ./implementations/java/benchmarks/matrix.compression-pgma.txt")
    lines.append("#")
    lines.append("# Every line: -c (full encode), --no-iter-save. -p/-P paired: 1, 2, 4, 8, 16.")
    lines.append("# Iterations: see ITERATIONS in generate_compression_pgma_matrix.py (default 10).")
    lines.append(f"# Runs: {n} (stems × geometries × iterations × parallelism).")
    lines.append("")

    for r, d in GEOMETRIES:
        extra = ""
        if r > 0 and d % r == 0:
            extra = f" (D = {d // r}×R)"
        lines.append(f"# ========== range {r}, domain {d}{extra} ==========")
        for stem in STEMS:
            lines.append(f"# --- {stem} ---")
            for it in ITERATIONS:
                for p, p_dec in PARALLEL_PAIRS:
                    parts = [stem, "-i", str(it), "-c", "-r", str(r), "-d", str(d), "-p", str(p), "-P", str(p_dec)]
                    parts.append("--no-iter-save")
                    lines.append(" ".join(parts))
            lines.append("")

    out = "\n".join(lines).rstrip() + "\n"
    with open(OUT_FILE, "w", encoding="utf-8") as f:
        f.write(out)
    print(f"Wrote {OUT_FILE} ({n} runs)")


if __name__ == "__main__":
    main()
