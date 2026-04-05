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

From the Java module directory:

```bash
cd implementations/java/compression
chmod +x run.sh   # once, if needed
./run.sh ../../data/images/lena.ppm
```

Pass any PPM path as the first argument. Outputs are written under `processes/<image-stem>/` at repo level (`codebook_r{r}_d{d}.fc`, `original.pgm`), with per-run iteration PGMs and `benchmark.csv` under `processes/<image-stem>/iterations/` (when the Java module’s working directory matches `run.sh`).
