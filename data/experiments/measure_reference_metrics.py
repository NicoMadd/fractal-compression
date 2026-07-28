#!/usr/bin/env python3
"""Measure quality, rate and timing of the PNG/JPEG references.

For every image of the experiment set the script compares each reference
(PNG and JPEG at the four quality levels) against the PGM ASCII original and
reports MSE, MAE, PSNR, bits per pixel, compression ratio against the
uncompressed 8 bpp image, and encode/decode times.

Metric definitions match docs/paper/materiales_y_metodos.tex: PSNR uses a
fixed 255-level dynamic range and the ratio is 8 / bpp, not the size of the
PGM ASCII input. Times are the median of TIMING_REPEATS in-memory encode and
decode operations with Pillow, which is the same library that produced the
reference files.

Output: data/experiments/reference_metrics.json
"""

from __future__ import annotations

import io
import json
import math
import statistics
import time
from pathlib import Path

import numpy as np
from PIL import Image

REPO_ROOT = Path(__file__).resolve().parents[2]
OUTPUT_ROOT = Path(__file__).resolve().parent
IMAGES_ROOT = OUTPUT_ROOT / "images"
MANIFEST_PATH = OUTPUT_ROOT / "assets_manifest.json"
RESULT_PATH = OUTPUT_ROOT / "reference_metrics.json"

JPEG_QUALITIES = (25, 50, 75, 90)
TIMING_REPEATS = 11
MAX_LEVEL = 255


def read_pgm_ascii(path: Path) -> np.ndarray:
    tokens: list[str] = []
    with path.open("r", encoding="ascii") as handle:
        for line in handle:
            comment = line.find("#")
            if comment != -1:
                line = line[:comment]
            tokens.extend(line.split())

    if not tokens or tokens[0] != "P2":
        raise ValueError(f"{path}: expected a P2 (PGM ASCII) magic number")

    width, height = int(tokens[1]), int(tokens[2])
    pixels = np.array(tokens[4:4 + width * height], dtype=np.int32)
    return pixels.reshape(height, width)


def quality_metrics(original: np.ndarray, decoded: np.ndarray) -> dict[str, float | None]:
    diff = original.astype(np.float64) - decoded.astype(np.float64)
    mse = float(np.mean(diff ** 2))
    mae = float(np.mean(np.abs(diff)))
    psnr = None if mse == 0 else 10.0 * math.log10(MAX_LEVEL ** 2 / mse)
    return {"mse": mse, "mae": mae, "psnr": psnr}


def time_codec(image: Image.Image, **save_kwargs) -> tuple[float, float, bytes]:
    encode_times: list[float] = []
    decode_times: list[float] = []
    payload = b""

    for _ in range(TIMING_REPEATS):
        buffer = io.BytesIO()
        start = time.perf_counter()
        image.save(buffer, **save_kwargs)
        encode_times.append((time.perf_counter() - start) * 1000.0)
        payload = buffer.getvalue()

        start = time.perf_counter()
        Image.open(io.BytesIO(payload)).load()
        decode_times.append((time.perf_counter() - start) * 1000.0)

    return statistics.median(encode_times), statistics.median(decode_times), payload


def measure(stem: str, original: np.ndarray) -> list[dict]:
    height, width = original.shape
    pixels = width * height
    source = Image.fromarray(np.clip(original, 0, 255).astype(np.uint8), mode="L")
    records: list[dict] = []

    variants = [("PNG", "png", IMAGES_ROOT / "png" / f"{stem}.png", {"format": "PNG"}, None)]
    for quality in JPEG_QUALITIES:
        variants.append((
            f"JPEG {quality}",
            "jpeg",
            IMAGES_ROOT / "jpeg" / f"{stem}_q{quality}.jpeg",
            {"format": "JPEG", "quality": quality, "subsampling": 0},
            quality,
        ))

    for label, fmt, path, save_kwargs, quality in variants:
        decoded = np.asarray(Image.open(path).convert("L"), dtype=np.int32)
        metrics = quality_metrics(original, decoded)

        encode_ms, decode_ms, payload = time_codec(source, **save_kwargs)
        size_bytes = path.stat().st_size
        if len(payload) != size_bytes:
            # In-memory re-encode must reproduce the committed file byte count;
            # otherwise the timing does not correspond to the measured rate.
            raise ValueError(f"{path}: re-encoded size {len(payload)} != {size_bytes}")

        bpp = size_bytes * 8 / pixels
        records.append({
            "stem": stem,
            "method": label,
            "format": fmt,
            "quality": quality,
            "path": str(path.relative_to(REPO_ROOT)),
            "bytes": size_bytes,
            "bpp": round(bpp, 6),
            "compression_ratio": round(8 / bpp, 4),
            "mse": round(metrics["mse"], 6),
            "mae": round(metrics["mae"], 6),
            "psnr": None if metrics["psnr"] is None else round(metrics["psnr"], 4),
            "encode_ms": round(encode_ms, 4),
            "decode_ms": round(decode_ms, 4),
        })

    return records


def main() -> None:
    manifest = json.loads(MANIFEST_PATH.read_text(encoding="ascii"))
    results: list[dict] = []

    for entry in manifest:
        stem = entry["stem"]
        original = read_pgm_ascii(REPO_ROOT / entry["pgma"]["path"])
        for record in measure(stem, original):
            results.append(record)
            psnr = "inf" if record["psnr"] is None else f"{record['psnr']:.2f}"
            print(f"{stem:<24} {record['method']:<8} {record['bytes']:>7}B "
                  f"bpp={record['bpp']:.3f} ratio={record['compression_ratio']:.2f} "
                  f"psnr={psnr:>7} enc={record['encode_ms']:.2f}ms dec={record['decode_ms']:.2f}ms")

    payload = {
        "timing_repeats": TIMING_REPEATS,
        "timing_statistic": "median",
        "psnr_max_level": MAX_LEVEL,
        "records": results,
    }
    RESULT_PATH.write_text(json.dumps(payload, indent=2) + "\n", encoding="ascii")
    print(f"\nmetrics -> {RESULT_PATH.relative_to(REPO_ROOT)}")


if __name__ == "__main__":
    main()
