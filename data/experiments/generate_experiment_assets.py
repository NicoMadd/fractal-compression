#!/usr/bin/env python3
"""Build the experiment image set from the original PGM ASCII sources.

For every image in SOURCES the script writes, under ``data/experiments/images/``:

  pgma/<stem>.ascii.pgm   PGM ASCII (P2), optionally center-cropped
  png/<stem>.png          lossless PNG
  jpeg/<stem>_q<Q>.jpg    JPEG at each quality in JPEG_QUALITIES

Pixel levels are copied verbatim from the source file. The PGM ``maxval`` is
kept in the header but never used to rescale samples, matching how the Java
reader (``PGMAImageMetadata``) interprets these files.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

from PIL import Image

REPO_ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = REPO_ROOT / "data" / "images" / "pgma"
OUTPUT_ROOT = Path(__file__).resolve().parent
IMAGES_ROOT = OUTPUT_ROOT / "images"

JPEG_QUALITIES = (25, 50, 75, 90)
PGM_LINE_WIDTH = 70


@dataclass(frozen=True)
class Source:
    filename: str
    stem: str
    self_similarity: str
    crop_to: int | None = None


SOURCES = (
    Source("apollonian_gasket.ascii.pgm", "apollonian_gasket_512", "alta", crop_to=512),
    Source("lena.ascii.pgm", "lena", "media"),
    Source("baboon.ascii.pgm", "baboon", "baja"),
)


@dataclass
class Raster:
    width: int
    height: int
    maxval: int
    pixels: list[int]


def read_pgm_ascii(path: Path) -> Raster:
    tokens: list[str] = []
    with path.open("r", encoding="ascii") as handle:
        for line in handle:
            comment = line.find("#")
            if comment != -1:
                line = line[:comment]
            tokens.extend(line.split())

    if not tokens or tokens[0] != "P2":
        raise ValueError(f"{path}: expected a P2 (PGM ASCII) magic number")

    width, height, maxval = (int(t) for t in tokens[1:4])
    pixels = [int(t) for t in tokens[4:]]
    expected = width * height
    if len(pixels) != expected:
        raise ValueError(f"{path}: expected {expected} samples, found {len(pixels)}")

    return Raster(width, height, maxval, pixels)


def center_crop(raster: Raster, size: int) -> Raster:
    if size > raster.width or size > raster.height:
        raise ValueError(f"cannot crop to {size}x{size} from {raster.width}x{raster.height}")

    top = (raster.height - size) // 2
    left = (raster.width - size) // 2

    cropped: list[int] = []
    for row in range(top, top + size):
        start = row * raster.width + left
        cropped.extend(raster.pixels[start:start + size])

    return Raster(size, size, raster.maxval, cropped)


def write_pgm_ascii(raster: Raster, path: Path, comment: str | None = None) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="ascii", newline="\n") as handle:
        handle.write("P2\n")
        if comment:
            handle.write(f"# {comment}\n")
        handle.write(f"{raster.width} {raster.height}\n{raster.maxval}\n")

        line = ""
        for value in raster.pixels:
            token = str(value)
            if line and len(line) + 1 + len(token) > PGM_LINE_WIDTH:
                handle.write(line + "\n")
                line = token
            else:
                line = f"{line} {token}" if line else token
        if line:
            handle.write(line + "\n")


def to_image(raster: Raster) -> Image.Image:
    image = Image.new("L", (raster.width, raster.height))
    image.putdata([min(255, max(0, value)) for value in raster.pixels])
    return image


def main() -> None:
    manifest = []

    for source in SOURCES:
        source_path = SOURCE_ROOT / source.filename
        raster = read_pgm_ascii(source_path)
        original_size = f"{raster.width}x{raster.height}"

        comment = f"{source.stem}: from {source.filename}"
        if source.crop_to:
            raster = center_crop(raster, source.crop_to)
            comment += f", center crop {original_size} -> {raster.width}x{raster.height}"

        pgm_path = IMAGES_ROOT / "pgma" / f"{source.stem}.ascii.pgm"
        write_pgm_ascii(raster, pgm_path, comment)

        image = to_image(raster)
        png_path = IMAGES_ROOT / "png" / f"{source.stem}.png"
        png_path.parent.mkdir(parents=True, exist_ok=True)
        image.save(png_path, format="PNG")

        jpegs = {}
        for quality in JPEG_QUALITIES:
            jpeg_path = IMAGES_ROOT / "jpeg" / f"{source.stem}_q{quality}.jpg"
            jpeg_path.parent.mkdir(parents=True, exist_ok=True)
            image.save(jpeg_path, format="JPEG", quality=quality, subsampling=0)
            jpegs[str(quality)] = {
                "path": str(jpeg_path.relative_to(REPO_ROOT)),
                "bytes": jpeg_path.stat().st_size,
                "bpp": round(jpeg_path.stat().st_size * 8 / (raster.width * raster.height), 6),
            }

        pixels = raster.width * raster.height
        manifest.append({
            "stem": source.stem,
            "source": str(source_path.relative_to(REPO_ROOT)),
            "source_size": original_size,
            "size": f"{raster.width}x{raster.height}",
            "maxval": raster.maxval,
            "self_similarity": source.self_similarity,
            "pgma": {
                "path": str(pgm_path.relative_to(REPO_ROOT)),
                "bytes": pgm_path.stat().st_size,
            },
            "png": {
                "path": str(png_path.relative_to(REPO_ROOT)),
                "bytes": png_path.stat().st_size,
                "bpp": round(png_path.stat().st_size * 8 / pixels, 6),
            },
            "jpeg": jpegs,
        })

        print(f"{source.stem:<24} {original_size} -> {raster.width}x{raster.height}  "
              f"pgm={pgm_path.stat().st_size}B png={png_path.stat().st_size}B "
              + " ".join(f"q{q}={jpegs[str(q)]['bytes']}B" for q in JPEG_QUALITIES))

    manifest_path = OUTPUT_ROOT / "assets_manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="ascii")
    print(f"\nmanifest -> {manifest_path.relative_to(REPO_ROOT)}")


if __name__ == "__main__":
    main()
