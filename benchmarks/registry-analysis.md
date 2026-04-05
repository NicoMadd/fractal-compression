# Registry analysis (`registry.jsonl`)

Interpretation of append-only [`registry.jsonl`](registry.jsonl). **Figures below match the current 36-line snapshot** (one JSON object per line; same `git_sha`, Java runner, GraalVM 25, macOS aarch64). Re-run the matrix and **refresh this file** when `registry.jsonl` changes.

**Memory:** *Peak heap (MB)* is `peak_heap_used_bytes` ÷ 1024² (JVM peak heap from the benchmark harness / `MemorySampler`, not host RSS).

## Experiment shape

| Aspect | Value |
|--------|--------|
| Lines | 36 |
| Images | `apollonian_gasket.ascii` (600×600 PGM), `baboon.ascii`, `lena.ascii`, `text-sample.ascii` (512×512) |
| Geometries | `(range, domain)` = (4,8), (8,16), (16,32) |
| Decode iterations | 10, 20, 30 |
| `clean_codebook` | `true` on each **−i 10** row per `(image, geometry)`; `false` on **−i 20** / **−i 30** (reuse codebook) |
| Encode when reuse | `compression_seconds` ~0.01–0.04 s (load only) |

Decode time scales with **pixels × iterations** (Apollonian is larger than 512²).

---

## Cross-image comparison (**i = 30**)

Same decode depth and the same stored codebook per `(stem, r, d)`. **Ratio** is `bytes_original_input / bytes_codebook` (dimensionless “compression ratio” of PGM vs `.fc`).

### Geometry **r = 4**, **d = 8**

| Image | PSNR (dB) | MSE | MAE | Peak heap (MB) | Ratio |
|--------|-----------|-----|-----|----------------|-------|
| Apollonian gasket | 30.85 | 53.51 | 1.44 | 315.5 | 2.49 |
| Baboon | 26.95 | 131.34 | 7.91 | 242.4 | 2.60 |
| Lena | 35.88 | 16.79 | 2.73 | 245.0 | 2.58 |
| Text sample | 31.01 | 51.55 | 1.31 | 255.8 | 3.08 |

### Geometry **r = 8**, **d = 16**

| Image | PSNR (dB) | MSE | MAE | Peak heap (MB) | Ratio |
|--------|-----------|-----|-----|----------------|-------|
| Apollonian gasket | 26.55 | 144.05 | 3.31 | 281.0 | 9.91 |
| Baboon | 21.32 | 479.34 | 15.47 | 235.3 | 10.38 |
| Lena | 30.92 | 52.57 | 4.40 | 240.8 | 10.34 |
| Text sample | 25.23 | 195.20 | 3.02 | 235.7 | 12.31 |

### Geometry **r = 16**, **d = 32**

| Image | PSNR (dB) | MSE | MAE | Peak heap (MB) | Ratio |
|--------|-----------|-----|-----|----------------|-------|
| Apollonian gasket | 18.99 | 821.24 | 8.63 | 282.4 | 40.45 |
| Baboon | 19.30 | 764.25 | 20.15 | 245.0 | 41.63 |
| Lena | 26.33 | 151.33 | 7.39 | 245.1 | 41.37 |
| Text sample | 23.53 | 288.72 | 4.25 | 251.9 | 49.00 |

---

## Apollonian gasket

*Stem: `apollonian_gasket.ascii` (600×600).*

### Quality

| r / d | PSNR i10 | MSE i10 | MAE i10 | PSNR i20 | MSE i20 | MAE i20 | PSNR i30 | MSE i30 | MAE i30 |
|-------|----------|---------|---------|----------|---------|---------|----------|---------|---------|
| 4 / 8 | 30.21 | 62.01 | 1.53 | 30.62 | 56.37 | 1.46 | 30.85 | 53.51 | 1.44 |
| 8 / 16 | 26.68 | 139.67 | 3.26 | 26.55 | 143.95 | 3.31 | 26.55 | 144.05 | 3.31 |
| 16 / 32 | 18.96 | 825.27 | 8.65 | 18.97 | 824.41 | 8.63 | 18.99 | 821.24 | 8.63 |

### Performance

*Encode i10 is full codebook build (−c). Encode i20 / i30 ≈ load-only reuse.*

| r / d | Comp i10 (s) | Dec i10 (s) | Peak i10 (MB) | Comp i20 (s) | Dec i20 (s) | Peak i20 (MB) | Comp i30 (s) | Dec i30 (s) | Peak i30 (MB) | Ratio |
|-------|--------------|-------------|---------------|--------------|-------------|---------------|--------------|-------------|---------------|-------|
| 4 / 8 | 220.16 | 14.70 | 277.1 | 0.03 | 26.79 | 285.3 | 0.04 | 39.18 | 315.5 | 2.49 |
| 8 / 16 | 34.47 | 14.48 | 230.7 | 0.02 | 26.90 | 256.4 | 0.02 | 38.80 | 281.0 | 9.91 |
| 16 / 32 | 6.90 | 14.77 | 249.6 | 0.01 | 26.79 | 266.8 | 0.01 | 39.27 | 282.4 | 40.45 |

---

## Baboon

*Stem: `baboon.ascii`.*

### Quality

| r / d | PSNR i10 | MSE i10 | MAE i10 | PSNR i20 | MSE i20 | MAE i20 | PSNR i30 | MSE i30 | MAE i30 |
|-------|----------|---------|---------|----------|---------|---------|----------|---------|---------|
| 4 / 8 | 27.00 | 129.83 | 7.87 | 26.95 | 131.36 | 7.92 | 26.95 | 131.34 | 7.91 |
| 8 / 16 | 21.33 | 478.87 | 15.46 | 21.32 | 479.32 | 15.47 | 21.32 | 479.34 | 15.47 |
| 16 / 32 | 19.30 | 764.29 | 20.15 | 19.30 | 764.22 | 20.15 | 19.30 | 764.25 | 20.15 |

### Performance

| r / d | Comp i10 (s) | Dec i10 (s) | Peak i10 (MB) | Comp i20 (s) | Dec i20 (s) | Peak i20 (MB) | Comp i30 (s) | Dec i30 (s) | Peak i30 (MB) | Ratio |
|-------|--------------|-------------|---------------|--------------|-------------|---------------|--------------|-------------|---------------|-------|
| 4 / 8 | 125.98 | 10.86 | 257.3 | 0.04 | 19.65 | 235.5 | 0.03 | 28.55 | 242.4 | 2.60 |
| 8 / 16 | 20.35 | 10.62 | 232.6 | 0.02 | 19.67 | 240.5 | 0.02 | 28.36 | 235.3 | 10.38 |
| 16 / 32 | 4.19 | 10.50 | 228.1 | 0.01 | 19.76 | 239.5 | 0.01 | 28.27 | 245.0 | 41.63 |

---

## Lena

*Stem: `lena.ascii`.*

### Quality

| r / d | PSNR i10 | MSE i10 | MAE i10 | PSNR i20 | MSE i20 | MAE i20 | PSNR i30 | MSE i30 | MAE i30 |
|-------|----------|---------|---------|----------|---------|---------|----------|---------|---------|
| 4 / 8 | 36.30 | 15.23 | 2.60 | 35.88 | 16.80 | 2.73 | 35.88 | 16.79 | 2.73 |
| 8 / 16 | 30.95 | 52.21 | 4.38 | 30.92 | 52.57 | 4.40 | 30.92 | 52.57 | 4.40 |
| 16 / 32 | 26.35 | 150.80 | 7.37 | 26.33 | 151.34 | 7.39 | 26.33 | 151.33 | 7.39 |

### Performance

| r / d | Comp i10 (s) | Dec i10 (s) | Peak i10 (MB) | Comp i20 (s) | Dec i20 (s) | Peak i20 (MB) | Comp i30 (s) | Dec i30 (s) | Peak i30 (MB) | Ratio |
|-------|--------------|-------------|---------------|--------------|-------------|---------------|--------------|-------------|---------------|-------|
| 4 / 8 | 124.36 | 10.63 | 251.8 | 0.04 | 19.87 | 240.2 | 0.03 | 28.65 | 245.0 | 2.58 |
| 8 / 16 | 20.34 | 10.75 | 234.4 | 0.02 | 19.52 | 242.2 | 0.02 | 28.49 | 240.8 | 10.34 |
| 16 / 32 | 4.18 | 10.57 | 231.5 | 0.04 | 19.66 | 246.9 | 0.01 | 28.29 | 245.1 | 41.37 |

---

## Text sample

*Stem: `text-sample.ascii`.*

### Quality

| r / d | PSNR i10 | MSE i10 | MAE i10 | PSNR i20 | MSE i20 | MAE i20 | PSNR i30 | MSE i30 | MAE i30 |
|-------|----------|---------|---------|----------|---------|---------|----------|---------|---------|
| 4 / 8 | 30.65 | 55.99 | 1.36 | 31.02 | 51.46 | 1.31 | 31.01 | 51.55 | 1.31 |
| 8 / 16 | 25.25 | 194.13 | 3.00 | 25.23 | 195.20 | 3.01 | 25.23 | 195.20 | 3.02 |
| 16 / 32 | 23.55 | 287.28 | 4.25 | 23.53 | 288.72 | 4.25 | 23.53 | 288.72 | 4.25 |

### Performance

| r / d | Comp i10 (s) | Dec i10 (s) | Peak i10 (MB) | Comp i20 (s) | Dec i20 (s) | Peak i20 (MB) | Comp i30 (s) | Dec i30 (s) | Peak i30 (MB) | Ratio |
|-------|--------------|-------------|---------------|--------------|-------------|---------------|--------------|-------------|---------------|-------|
| 4 / 8 | 113.23 | 10.61 | 253.0 | 0.03 | 19.63 | 230.9 | 0.03 | 28.44 | 255.8 | 3.08 |
| 8 / 16 | 18.49 | 10.69 | 238.7 | 0.02 | 19.48 | 244.1 | 0.02 | 28.27 | 235.7 | 12.31 |
| 16 / 32 | 4.08 | 10.55 | 245.6 | 0.01 | 19.44 | 239.8 | 0.01 | 28.42 | 251.9 | 49.00 |

---

## Hypotheses

1. **Quality vs rate:** Block geometry (`range_size` / `domain_size`) moves PSNR and ratio more than 10 vs 30 decode iterations. Coarse r16/d32 yields high **Ratio** but poor **MSE/PSNR** for texture and edges (baboon, Apollonian detail).

2. **Iteration curves:** Apollonian r4/d8 improves with **i**; Lena r4/d8 shows a small quality dip after i10—consistent with **random decode initialization** and non-monotone MSE under fixed iteration budgets.

3. **Reuse runs:** Near-zero **Comp i20/i30** with `clean_codebook: false` matches “load codebook, decode only.”

4. **CPU fields in JSON:** `process_cpu_load` / `system_cpu_load` are often zero in this snapshot—treat **peak heap** as the more trustworthy load proxy unless CPU sampling is fixed upstream.

5. **Cross-image ranking at i30:** For each geometry, **Lena** leads PSNR; **Baboon** trails (hard texture). **Ratio** ordering follows codebook size per geometry more than image identity.

## Suggested improvements

| Area | Idea |
|------|------|
| Registry | Optional `decode_seed` / codebook path for reproducibility. |
| Experiments | Multiple seeds or deterministic initial image; log MSE vs iteration (see per-run `benchmark.csv`). |
| Comparison | Normalize decode time by **pixel count** when mixing 600² and 512². |
| Algorithm | Adaptive partitions / denser domains for better PSNR at mid **bytes_codebook**. |

## Conclusions

- The registry encodes a full **4 × 3 × 3** design with a clear **encode-vs-reuse** split.
- **Cross-image tables at i = 30** summarize quality (**PSNR / MSE / MAE**), **peak heap**, and **ratio** on a common footing.
- **Per-image** tables separate **quality** (error metrics across geometries and iterations) from **performance** (encode/decode time, peak heap, ratio).
- For thesis figures, plot **rate–distortion** (PSNR vs `bytes_codebook`) and **time** alongside these tables; refresh this markdown when `registry.jsonl` is extended.
