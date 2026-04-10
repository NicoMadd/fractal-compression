# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **`--debug`:** verbose stdout (phase/progress logs). Default is **summary-only** (`--- Summary ---` with timings, ratio, metrics, output paths).
- **Benchmark manifest / summary:** **`ratio_original_over_zip_codebook`** (`bytes_original_input / zip_bytes_codebook`) and a second **compression ratio** line vs zipped codebook on stdout.

### Changed

- **Codebook path:** per-geometry **`codebook_r{r}_d{d}.fc`** now lives under **`processes/<stem>/codebooks/`** (see **`Codebook.CODEBOOKS_SUBDIR`** / **`Codebook.pathForGeometry`**).

## [0.5.0] - 2026-04-09 — concurrent pipeline & benchmark matrices (`feature/5-concurrent-pipeline`)

### Added

- **Benchmark registry / manifest** ([`benchmarks/RUN_RECORD.md`](benchmarks/RUN_RECORD.md)): **`compression_parallelism`**, **`decompression_parallelism`**; **`skip_compression`**, **`skip_decompression`**; optional decode breakdown (**`decompression_{decode,save,snapshot_metrics}_seconds`**, **`decode_iteration_{avg,min,max}_seconds`**).
- **`PipelineParams` / `run.sh`:** **`-p`** / **`-P`** thread pools; **`--no-iter-save`**; **`--mr`** / **`--br`**; **`--skip-compression`** / **`-sc`** and **`--skip-decompression`** / **`-sd`** (`run.sh` rejects **`-c`** with **`-sc`** and both skips together).
- **`PGMAPipeline`:** honors skip flags; encode-only runs omit reconstruction PNG baseline when decode is skipped.
- **PGMA benchmark matrices:** **`matrix.compression-pgma.txt`** (generated: **`generate_compression_pgma_matrix.py`** — **lena** only, **10** **(r,d)** pairs, **`-p`/`-P`** **1…16**, **`-i` 10**, every line **`-c`**); **`matrix.decompression-pgma.txt`** (multi-image decode sweep, **`-p` 8**); index **`matrix.all-pgma.txt`**; **`run_pgma_matrices.sh`** runs compression then decompression matrix.
- **`implementations/java/benchmarks/run_matrix.sh`:** copies **`benchmarks/registry.jsonl`** to **`benchmarks/snapshots/registry-<timestamp>.jsonl`** after a full matrix pass.

### Changed

- **`BicubicReductionStrategy.reduce`:** power-of-two shrink via repeated **2:1** separable Catmull–Rom; non-power-of-two ratios throw (use **mean** or change geometry).
- **PGMA matrix docs / samples:** replace monolithic **`matrix.all-pgma.example.txt`** with **`matrix.all-pgma.txt`** index + split matrix files; update **`matrix.example.txt`**, [**`benchmarks/README.md`**](benchmarks/README.md), **`.cursor/rules/project-context.mdc`**.

## [0.4.0] - 2026-04-05 — basic benchmark registry (`feature/4-benchmark-registry`)

### Added

- **`TransformationFactory`:** `of(TransformationType)` for all symmetries; reflection implementations **HZ**, **VC**, **FD**, **SD**; rotation classes **CW90**, **CW180**, **CCW90**.
- **`GrayBlockCompression`:** **`ALLOWED_TRANSFORMATIONS`** covers every **`TransformationType`** via **`Arrays.stream(...).map(TransformationFactory::of)`**.
- **`FractalMapping.transformation()`** resolves a **`Transformation`** through **`TransformationFactory`**.
- **Sample assets:** **`data/images/text-sample.png`** and **`data/images/pgma/text-sample.ascii.pgm`** (512×512 **P2** PGMA, grayscale on white) for high-contrast / text-like pipeline runs.
- **Benchmark harness:** `run_manifest.json` per run (sizes: input PGM, codebook, **PNG** gray baseline, **zip DEFLATE** archives for original vs codebook); baselines under `processes/<stem>/baselines/`; **MAE** in `benchmark.csv`; **MemorySampler** peak and **average** heap/runtime; nested **`host`** (CPU, OS, RAM, JVM); **`schema_version`** + **`runner`** on each JSON line per [`benchmarks/RUN_RECORD.md`](benchmarks/RUN_RECORD.md); append-only [`benchmarks/registry.jsonl`](benchmarks/registry.jsonl); **`benchmarks/README.md`**; matrix driver [`implementations/java/benchmarks/run_matrix.sh`](implementations/java/benchmarks/run_matrix.sh); env: `BENCHMARK_GIT_SHA`, optional `BENCHMARK_RUNNER_ID`, `BENCHMARK_RUNNER_VERSION`.
- Java **`src/benchmarks/`:** `BenchmarkRegistry`, `RunManifest`, `RunnerInfo`, `HostEnvironmentMetrics`, `MemorySampler`, `ImageErrorMetrics`; **`CompressionBaselines`** (PNG + zip).
- **[`benchmarks/registry-analysis.md`](benchmarks/registry-analysis.md)** — per-image and cross-image tables over `registry.jsonl` (refresh when the snapshot changes).

### Changed

- **`PGMAPipeline`:** append registry line after successful runs; write manifest and baselines; **`Codebook.pathForGeometry`** → on-disk **`codebook_r{r}_d{d}.fc`** (no legacy **`codebook.fc`**). **`-c`** forces rebuild. Zip entry name matches the codebook file.
- **`PGMAPipeline`:** when the codebook has **≥ 800** mappings, ~**25** **per-iteration** “applying mappings **k**/total” lines so long decode passes stay visible.
- **`GrayBlockCompression`:** **progress logging** — geometry summary, phase labels (range blocks, domain blocks, bicubic domain shrink, search), and ~**25** updates while matching range blocks.
- **PG matrix sample:** [`implementations/java/benchmarks/matrix.all-pgma.example.txt`](implementations/java/benchmarks/matrix.all-pgma.example.txt) — square PGMAs; **36** runs (4 × 3 geometries × 3 iteration counts); **−c** on **−i 10** when geometry is new per image; **−i 20** / **30** reuse codebooks.
- **`FileUtils`:** `getFileSize(Path)`; generic **iterations-dir** javadoc (no codebook-specific names).
- Root **`README.md`** and **`.cursor/rules/project-context.mdc`** — `benchmarks/` layout and runner env vars.
- **`implementations/java/compression/run.sh`:** usage text for **`-c`** references per-geometry **`codebook_r{r}_d{d}.fc`**.

### Fixed

- **`PGMAPipeline`:** decode applies the codebook isometry to the reduced domain in a **scratch** `GrayPixel[][]`, then **`s` / `o`**, matching encode (no overwrite with untransformed samples).

## [0.3.0] - 2026-04-04 — `feature/3-bicubic-domain-reduce`

### Added

- **Fractal domain transforms:** `Transformation`, `TransformationType`, `Identity`; gray block search applies each allowed transform to the reduced domain before the affine `s`/`o` fit.
- **`MatrixUtils.copy`**, **`MatrixUtils.shape`**, and **`MatrixShape`** for rectangular `T[][]` helpers.
- `docs/fractal-codebook-fc.html` — **Fractal Codebook** ASCII format spec (PGMA-style; magic `FC`)
- **Fractal Codebook** Java types: `FractalMapping`, `Codebook` (FC read/write); `SequenceWriter`; `SequenceReader` moved to `utils/files/readers/`
- `FileUtils.getFileSize`; `PGMAPipeline` prints compression ratio **N:1** (original image file size vs `codebook.fc`)
- **`SequenceOutput` / `SequenceInput`** (shared write/read API); **`BytesWriter` / `BytesReader`** (big-endian binary); **`SequenceReader(String path)`**; `SequenceWriter` and `SequenceReader` implement the text backends for those interfaces.
- **`MatrixUtils`:** `copySquare` (bounds clamp, edge replication), `floatMap`, `dotProduct`, `avgKernel`, `fill(float)`, row-wise `print` for generic and `float[][]` matrices.

### Changed

- **Summary:** **Bicubic** domain reduction; **Fractal Codebook** and **processes/** output layout updates (**breaking** for older `.fc` trees); **CLI** / **run.sh** / **PGMAPipeline** ergonomics; **2D** affine fit on **transformed** reduced-domain pixels and **transformation** ordinal persisted in codebook rows.
- **`FractalMapping` / `GrayCompressedBlock`:** include **`TransformationType`** `t`; **`Codebook`** mapping rows append **`t`** as a decimal **ordinal** after **`s`** and **`o`** (older `.fc` files with only six affine tokens per row must be re-saved).
- **`GrayBlockCompression`:** `s`/`o` and error use **2D** range and transformed-domain pixels (`calculateS` on matrices); best block stores the winning transform type.
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
