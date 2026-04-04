# Bitácora (project log)

Informal benchmark and run notes. Add dated entries as you go.

## 2026-04-02 — Lena ASCII (reference snapshot)

**Command (from terminal log):**

```bash
./implementations/java/compression/run.sh lena.ascii -i 10 -c -d 16 -r 8
```

**Parameters:** iterations `10`, clean codebook (`-c`), domain `16`, range `8`, image `lena.ascii`.

**Final metrics (after last iteration; values printed at end of run):**

| Metric | Value |
|--------|--------|
| MSE | 64.21495819091797 |
| PSNR | 30.054441567383265 |
| Compression time | 3.819825916 s |
| Decompression time | 11.017722834 s |
| Compression ratio | 11.08:1 |

*Baseline snapshot up to this date; later runs may differ with image, flags, or code changes.*

## 2026-04-02 — Lena ASCII (range 16 / domain 32)

**Command (from terminal log):**

```bash
./implementations/java/compression/run.sh lena.ascii -i 10 -c -d 32 -r 16
```

**Parameters:** iterations `10`, clean codebook (`-c`), domain `32`, range `16`, image `lena.ascii`.

**Final metrics (after last iteration; values printed at end of run):**

| Metric | Value |
|--------|--------|
| MSE | 182.0757179260254 |
| PSNR | 25.528283298115788 |
| Compression time | 0.982418667 s |
| Decompression time | 10.185761583 s |
| Compression ratio | 44.45:1 |

*Snapshot from the same session log; larger blocks trade ratio for reconstruction quality vs. the 8×16 run above.*

## 2026-04-02 — Lena ASCII (reuse codebook, 16 / 32)

**Command (from terminal log):**

```bash
./implementations/java/compression/run.sh lenascii -i 10 -d 32 -r 16
```

**Parameters:** iterations `10`, domain `32`, range `16`; **no** `-c` (log shows **Reading codebook** — existing `codebook.fc`). Name as typed: `lenascii` (matches `lena.ascii*` under `data/images/pgma`).

**Final metrics (after last iteration; values printed at end of run):**

| Metric | Value |
|--------|--------|
| MSE | 182.83777236938477 |
| PSNR | 25.510144395084243 |
| Compression time | 0.007152917 s |
| Decompression time | 10.225091625 s |
| Compression ratio | 44.45:1 |

*Compression time is near-zero because the codebook was not rebuilt. MSE/PSNR differ slightly from the **-c** run with the same r/d flags above — pipeline path and/or floating accumulation differ.*

## 2026-04-02 — Lena ASCII (range 4 / domain 8, rebuild)

**Command (from terminal log):**

```bash
./implementations/java/compression/run.sh lena.ascii -i 10 -c -d 8 -r 4
```

**Parameters:** iterations `10`, clean codebook (`-c`), domain `8`, range `4`, image `lena.ascii`. Smaller blocks → larger codebook, lower compression ratio, better PSNR.

**Final metrics (after last iteration; values printed at end of run):**

| Metric | Value |
|--------|--------|
| MSE | 18.852638244628906 |
| PSNR | 35.37708226755254 |
| Compression time | 13.962638625 s |
| Decompression time | 10.326542208 s |
| Compression ratio | 2.76:1 |

## 2026-04-04 — Lena ASCII (range 8 / domain 16, full `TransformationType` search)

**Command (from terminal log):**

```bash
./implementations/java/compression/run.sh lena.ascii -i 10 -c -d 16 -r 8
```

**Parameters:** iterations `10`, clean codebook (`-c`), domain `16`, range `8`, image `lena.ascii`. Encoder tries every **`TransformationType`** (identity, rotations, reflections); decode applies the stored isometry before **`s` / `o`**.

**Final metrics (after last iteration; values printed at end of run):**

| Metric | Value |
|--------|--------|
| MSE | 52.21171188354492 |
| PSNR | 30.953124280596267 |
| Compression time | 20.755891292 s |
| Decompression time | 10.79434675 s |
| Compression ratio | 10.34:1 |

*Same CLI as the 2026-04-02 r8/d16 row above; metrics differ because the compressor search space and decode path now include all eight block symmetries (longer compression time).*
