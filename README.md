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

# force codebook rebuild (see codebook_r{r}_d{d}.fc)
./run.sh lena -c
```

Anything after `--` is forwarded to `Main` (same argument model as `PipelineParams` in the Java sources).

Outputs go under repo-root `processes/<stem>/`: `codebook_r{r}_d{d}.fc`, `original.pgm`, and under `iterations/` the iteration PGMs plus `benchmark.csv`.
