# Fractal Compression Benchmarking

## Author

Im Nicolás Madeo, Software Engineer from UTN ( Universidad Tecnológica Nacional). Im currently pursuing a masters degree in Software Engineering at UNLP ( Universidad Nacional de La Plata). 

I'll be using this repository as proof for my thesis: _Comparative analysis on fractal compression algorithms with GPGPU acceleration and artificial intelligence integrations._

## Introduction

This repository holds information about analysis, code and performance benchmarks about compression algorithms. These algorithms may vary from general purpose and applications to many others, focusing primarily on fractal compression algorithms. 

Implementations may be made in Java, C++ and Python.

## Layout

| Path | Purpose |
|------|--------|
| `algorithms/` | High-level algorithm description |
| `benchmarks/` | Cross-language **`RUN_RECORD.md`** + append-only **`registry.jsonl`** |
| `data/images/` | Sample PPM inputs (e.g. `lena.ppm`, `baboon.ppm`) |
| `implementations/java/compression/` | Java reference implementation |

## Requirements

- **Java** (JDK with `javac` / `java`)

## Run (Java)

From the Java module directory, `run.sh` compiles and runs the pipeline on a PPM under `data/images/` (default subfolder: `pgma`). The first argument is the **file stem** (prefix); it must match exactly one file like `stem*.ppm` in that folder.

```bash
cd implementations/java/compression
chmod +x run.sh   # once, if needed

# default type pgma, default -i 25 -r 4 -d 8
./run.sh lena

# explicit image type folder under data/images
./run.sh pgma baboon

# overrides: iterations, range block size, domain block size
./run.sh lena -i 40 -r 8 -d 16

# force codebook rebuild (see codebooks/codebook_r{r}_d{d}.fc)
./run.sh lena -c
```

Anything after `--` is forwarded to `Main` (same argument model as `PipelineParams` in the Java sources).

Outputs go under repo-root `processes/<stem>/`: `codebooks/codebook_r{r}_d{d}.fc`, `original.pgm`, and under `iterations/` the iteration PGMs plus `benchmark.csv`.

## Reproduce the paper experiment

Run these commands from the repository root to regenerate the experiment assets, run the fractal-compression matrix, measure the PNG/JPEG references, and assemble the results:

```bash
python3 data/experiments/generate_experiment_assets.py
bash data/experiments/run_fractal_experiments.sh
python3 data/experiments/measure_reference_metrics.py
python3 data/experiments/summarize_results.py
```

### Dependencies

- **Java JDK** with `java` and `javac` available on `PATH`. The paper's runs used OpenJDK 25.0.1 / GraalVM CE 25.0.1+8.1 (HotSpot).
- **Python 3** and `pip`.
- **Pillow** is used to generate the PNG/JPEG assets and measure their in-memory encode/decode times.
- **NumPy** is used to calculate the PNG/JPEG reference metrics. The summary script uses only the Python standard library.

To install the Python packages in a virtual environment:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install Pillow numpy
```

This repository does not pin Python or package versions. The reference-measurement script checks that an in-memory re-encode has the same byte count as the generated reference file, so using a different Pillow version may affect that check.

### Experiment steps and outputs

1. `generate_experiment_assets.py` reads the source PGM files from `data/images/pgma/` and writes the frozen 512×512 PGM, PNG, and JPEG inputs plus `data/experiments/assets_manifest.json`.
2. `run_fractal_experiments.sh` runs the four images with the three geometries (`r=4,d=8`, `r=8,d=16`, and `r=16,d=32`), 20 decode iterations, 12 compression threads, and 12 decompression threads. It forces a fresh codebook for each run. Logs default to `processes/java/_experiment_logs/`; each run's `benchmark.csv` and `run_manifest.json` are archived under `data/experiments/results/<image>_r<range>_d<domain>/`.
3. `measure_reference_metrics.py` writes the PNG/JPEG measurements to `data/experiments/reference_metrics.json`.
4. `summarize_results.py` combines the archived FBC runs and reference metrics, writes `data/experiments/results_summary.json`, and prints the corresponding LaTeX rows to standard output. It does not edit the paper source.
