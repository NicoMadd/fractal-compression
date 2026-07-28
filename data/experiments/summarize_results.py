#!/usr/bin/env python3
"""Aggregate the experiment results into a single summary.

Reads the archived runs under data/experiments/results/<stem>_r<r>_d<d>/
(benchmark.csv + run_manifest.json) produced by run_fractal_experiments.sh,
together with data/experiments/reference_metrics.json, and writes
data/experiments/results_summary.json plus the LaTeX rows used in
docs/paper/resultados.tex.

Rate for the fractal method is the size of the codebook file that the encoder
actually writes to disk (the .fc produced by the Java pipeline). The
compression ratio is measured against the uncompressed 8-bit image, that is
one byte per pixel, so that every method in the comparison shares the same
reference.
"""

from __future__ import annotations

import csv
import json
import re
from pathlib import Path

OUTPUT_ROOT = Path(__file__).resolve().parent
RESULTS_DIR = OUTPUT_ROOT / "results"
MANIFEST_PATH = OUTPUT_ROOT / "assets_manifest.json"
REFERENCE_PATH = OUTPUT_ROOT / "reference_metrics.json"
SUMMARY_PATH = OUTPUT_ROOT / "results_summary.json"

REPORTED_ITERATIONS = (5, 10, 20)
DIR_PATTERN = re.compile(r"^(?P<stem>.+)_r(?P<range>\d+)_d(?P<domain>\d+)$")


def codebook_cost(width: int, height: int, range_size: int, domain_size: int,
                  codebook_bytes: int) -> dict:
    bpp = codebook_bytes * 8 / (width * height)
    return {
        "range_blocks": (width // range_size) * (height // range_size),
        "domain_blocks": (width // domain_size) * (height // domain_size),
        "bytes": codebook_bytes,
        "bpp": round(bpp, 6),
        "compression_ratio": round(8 / bpp, 4),
    }


def read_iteration_metrics(path: Path) -> dict[int, dict[str, float]]:
    metrics: dict[int, dict[str, float]] = {}
    with path.open(newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            n = int(row["n"])
            if n not in REPORTED_ITERATIONS:
                continue
            metrics[n] = {
                "mse": round(float(row["mse"]), 6),
                "mae": round(float(row["mae"]), 6),
                "psnr": round(float(row["psnr"]), 4),
                "decode_pass_ms": round(float(row["durationSeconds"]) * 1000.0, 4),
            }
    return metrics


def collect_fractal(sizes: dict[str, tuple[int, int]]) -> list[dict]:
    records: list[dict] = []

    for directory in sorted(RESULTS_DIR.iterdir()):
        match = DIR_PATTERN.match(directory.name)
        if not directory.is_dir() or not match:
            continue

        stem = match.group("stem")
        range_size = int(match.group("range"))
        domain_size = int(match.group("domain"))
        width, height = sizes[stem]

        manifest = json.loads((directory / "run_manifest.json").read_text(encoding="utf-8"))
        iterations = read_iteration_metrics(directory / "benchmark.csv")
        cost = codebook_cost(width, height, range_size, domain_size,
                             manifest["bytes_codebook"])

        decode_seconds = manifest["decompression_decode_seconds"]
        records.append({
            "stem": stem,
            "method": f"FBC r={range_size}, d={domain_size}",
            "range_size": range_size,
            "domain_size": domain_size,
            "iterations": manifest["iterations"],
            "compression_parallelism": manifest["compression_parallelism"],
            "decompression_parallelism": manifest["decompression_parallelism"],
            "bytes": cost["bytes"],
            "bpp": cost["bpp"],
            "compression_ratio": cost["compression_ratio"],
            "range_blocks": cost["range_blocks"],
            "encode_ms": round(manifest["compression_seconds"] * 1000.0, 4),
            "decode_total_ms": round(decode_seconds * 1000.0, 4),
            "decode_pass_avg_ms": round(manifest["decode_iteration_avg_seconds"] * 1000.0, 4),
            "quality": {str(n): iterations[n] for n in sorted(iterations)},
        })

    return sorted(records, key=lambda r: (r["stem"], r["range_size"]))


def latex_rows(fractal: list[dict], references: list[dict], order: list[str]) -> list[str]:
    def number(value: float, decimals: int) -> str:
        return f"{value:,.{decimals}f}".replace(",", "\\,").replace(".", ",")

    labels = {
        "apollonian_gasket_512": "Apollonian",
        "lena": "Lena",
        "baboon": "Baboon",
        "sun_tzu_512": "Sun Tzu",
    }

    lines: list[str] = []
    for index, stem in enumerate(order):
        if index:
            lines.append("\\midrule")
        first = True
        for record in [r for r in fractal if r["stem"] == stem]:
            name = labels[stem] if first else ""
            first = False
            lines.append(
                f"{name:<11}& FBC $r{{=}}{record['range_size']}$ "
                f"& ${number(record['compression_ratio'], 2)}{{:}}1$ "
                f"& ${number(record['bpp'], 2)}$ "
                f"& ${number(record['quality']['5']['psnr'], 2)}$ "
                f"& ${number(record['quality']['10']['psnr'], 2)}$ "
                f"& ${number(record['quality']['20']['psnr'], 2)}$ "
                f"& ${number(record['encode_ms'], 0)}$ "
                f"& ${number(record['decode_total_ms'], 0)}$ \\\\"
            )
        for record in [r for r in references if r["stem"] == stem]:
            psnr = "$\\infty$" if record["psnr"] is None else f"${number(record['psnr'], 2)}$"
            lines.append(
                f"{'':<11}& {record['method']:<8} "
                f"& ${number(record['compression_ratio'], 2)}{{:}}1$ "
                f"& ${number(record['bpp'], 2)}$ "
                f"& --- "
                f"& --- "
                f"& {psnr} "
                f"& ${number(record['encode_ms'], 2)}$ "
                f"& ${number(record['decode_ms'], 2)}$ \\\\"
            )
    return lines


def main() -> None:
    assets = json.loads(MANIFEST_PATH.read_text(encoding="ascii"))
    sizes = {}
    order = []
    for entry in assets:
        width, height = (int(v) for v in entry["size"].split("x"))
        sizes[entry["stem"]] = (width, height)
        order.append(entry["stem"])

    fractal = collect_fractal(sizes)
    references = json.loads(REFERENCE_PATH.read_text(encoding="ascii"))["records"]

    summary = {
        "images": [
            {
                "stem": entry["stem"],
                "size": entry["size"],
                "self_similarity": entry["self_similarity"],
                "pgma_bytes": entry["pgma"]["bytes"],
            }
            for entry in assets
        ],
        "fractal": fractal,
        "references": references,
    }
    SUMMARY_PATH.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")

    header = (f"{'imagen':<24}{'método':<18}{'ratio':>9}"
              f"{'psnr5':>9}{'psnr10':>9}{'psnr20':>9}{'enc ms':>11}{'dec ms':>10}")
    print(header)
    print("-" * len(header))
    for stem in order:
        for record in [r for r in fractal if r["stem"] == stem]:
            quality = record["quality"]
            print(f"{stem:<24}{record['method']:<18}"
                  f"{record['compression_ratio']:>9.2f}"
                  f"{quality['5']['psnr']:>9.2f}"
                  f"{quality['10']['psnr']:>9.2f}{quality['20']['psnr']:>9.2f}"
                  f"{record['encode_ms']:>11.1f}{record['decode_total_ms']:>10.1f}")
        for record in [r for r in references if r["stem"] == stem]:
            psnr = float("inf") if record["psnr"] is None else record["psnr"]
            print(f"{stem:<24}{record['method']:<18}"
                  f"{record['compression_ratio']:>9.2f}{'':>9}{'':>9}{psnr:>9.2f}"
                  f"{record['encode_ms']:>11.2f}{record['decode_ms']:>10.2f}")

    print(f"\nsummary -> {SUMMARY_PATH.name}\n")
    print("\n".join(latex_rows(fractal, references, order)))


if __name__ == "__main__":
    main()
