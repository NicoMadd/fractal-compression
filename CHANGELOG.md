# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `docs/fractal-codebook-fc.html` — **Fractal Codebook** ASCII format spec (PGMA-style; magic `FC`)
- **Fractal Codebook** Java types: `FractalMapping`, `Codebook` (FC read/write); `SequenceWriter`; `SequenceReader` moved to `utils/files/readers/`
- `FileUtils.getFileSize`; `PGMAPipeline` prints compression ratio **N:1** (original image file size vs `codebook.fc`)
- **`SequenceOutput` / `SequenceInput`** (shared write/read API); **`BytesWriter` / `BytesReader`** (big-endian binary); **`SequenceReader(String path)`**; `SequenceWriter` and `SequenceReader` implement the text backends for those interfaces.
- **`MatrixUtils`:** `copySquare` (bounds clamp, edge replication), `floatMap`, `dotProduct`, `avgKernel`, `fill(float)`, row-wise `print` for generic and `float[][]` matrices.

### Changed

- **`PipelineParams.parse`:** optional range/domain positionals; defaults when omitted (range **4**, domain **2×** range); flags **`-r`**, **`-d`**, **`-c`** (rebuild codebook). Works with `Main` after `run.sh` or direct `java`.
- `implementations/java/compression/run.sh`: minimal **`run.sh <name>`** uses defaults **25** iterations, range **4**, domain **8**; **`-i`**, **`-r`**, **`-d`**, **`-c`**; **`--`** forwards extra args to `Main`.
- **`PGMAPipeline`:** when **`cleanCodebook`** is set (**`-c`**), ignores an existing **`codebook.fc`** and recomputes + saves.
- **`Codebook`:** I/O via **`SequenceInput`** / **`SequenceOutput`**; header and rows use structured reads/writes; **`s`** / **`o`** serialized with **`write(float, 2)`** (two fractional digits) for smaller ASCII rows.
- **`SequenceWriter`:** **`write(float, int precision)`** (`Locale.US`).
- **Fractal Codebook (`.fc`) header (breaking):** after magic `FC`, the first line must be three decimal integers — **range block size**, **domain block size**, and **mapping count** `N` — then `N` mapping rows. Older files with only `FC N` must be re-saved or hand-migrated. `Codebook` read/write and `rangeSize()` / `domainSize()` match this layout; see `docs/fractal-codebook-fc.html`.
- Output root renamed to **`processes/`** (was `iterations/`): per image `processes/<stem>/` holds `codebook.fc` and `original.pgm`; **`processes/<stem>/iterations/`** holds PG iteration frames and `benchmark.csv`. Only the inner `iterations` folder is wiped on each run. Sample outputs under `processes/` migrated from `iterations/`.
- `README.md`: documents the `processes/<stem>/` layout
- **`GrayBlock.reduce`:** domain shrinking uses **`GrayPixelUtils.bicubicReduction`** (separable Catmull–Rom 4×4 stencil, result clamped 0–255) instead of **`GrayPixelUtils.reduce`** (mean over a flattened tile).
- **`GrayPixelUtils`:** removed **`reduce`**; added **`bicubicReduction`** and **`meanReduction`**.

### Fixed

- `Codebook` file load: `deserialize` assigns `mappings`; path constructor initializes an empty list before read; failed deserialize logs and throws (no silent empty codebook)
- `PGMAPipeline`: load `codebook.fc` from `runDir` when present, else compress and save; `parseMappings` rebuilds `GrayRangeMatch` from `FractalMapping` (placeholder pixel grids; positions and `s`/`o` drive decode)
- **`GrayPixel`:** `IllegalArgumentException` for out-of-range levels includes the offending value (bicubic overshoot is clamped before construction, but the message helps when validation still fails).

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
