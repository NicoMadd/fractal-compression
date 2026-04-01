# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `docs/fractal-codebook-fc.html` — **Fractal Codebook** ASCII format spec (PGMA-style; magic `FC`)

## [0.2.0] - 2026-03-29 — `feature/2-gray-block-compression` (`main`)

### Added

- `GrayBlockCompression`, `GrayBlock`, and gray pixel / compression helper types
- `RGBBlockCompression` and `RGBBlock` (RGB path split from the old single block compressor)
- PGM/PGMA reading (`PGMAUtils`, `PGMAImageMetadata`) and related sample images under `data/images/`
- Fractal types reorganized under `block/`, `rangematch/`, `reducedpair/`, `compressed/`
- `SequenceReader`, per-input directories under `iterations/<image>/` with `benchmark.csv` (and generated PDFs where applicable)
- `TODO.md`

### Changed

- `Main` refactored for directory layout, iterations, and image buffers
- Pixel and PPM utilities moved under `utils/image/`
- **`run.sh`** updated for the per-input benchmarking workflow

### Removed

- Flat `iterations/*.ppm` tree and root-level `iterations/benchmark.csv` from the **0.1.0** layout

## [0.1.0] - 2026-03-25 — `feature/1-raw-block-compression`

### Added

- Algorithm notes under `algorithms/`
- Sample images under `data/images/` for the Java runner
- Java reference: **`BlockCompression`**, fractal types on `utils.fractal` (`Block`, `CompressedBlock`, `RangeMatch`, `ReducedPair`)
- PPM-oriented I/O (`PPMUtils`, `PPMImageMetadata`, pixel helpers)
- Flat **`iterations/`** output layout and baseline benchmarking
