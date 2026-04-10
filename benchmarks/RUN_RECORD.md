# Benchmark run record (JSONL)

One UTF-8 **line** = one **JSON object** describing a **single completed** compression/benchmark run. The file [`registry.jsonl`](registry.jsonl) appends these lines across sessions; each implementation may also write a copy as `run_manifest.json` under its run output directory.

This format is **implementation-agnostic**: Java, C++, Python, or other runners should emit the **same core field names** so tooling can merge results. Language-specific details go under **`runner`** and optional **`extras`**.

## Versioning

| Field | Meaning |
|--------|--------|
| **`schema_version`** | Integer. **1** = current layout. Increment only if a **breaking** rename/removal occurs; additive fields do not require a bump. |

Consumers **SHOULD** ignore unknown top-level keys. Producers **MAY** add **`extras`** (object) for keys that are not yet standardized.

## Required core fields (schema 1)

| Key | Type | Description |
|-----|------|--------------|
| `schema_version` | int | Always **1** for this spec. |
| `runner` | object | See below. |
| `epoch_ms` | int | Wall-clock end (or record) time, Unix ms. |
| `git_sha` | string | Repo revision; may be empty. |
| `source_image` | string | Path or URI to input image (runner-defined). |
| `run_dir` | string | Output directory for this run (runner-defined). |
| `iterations` | int | Decompression iterations. |
| `range_size` | int | Range block edge length (this project). |
| `domain_size` | int | Domain block edge length (this project). |
| `clean_codebook` | bool | Whether the codebook was rebuilt. |
| `skip_iteration_saves` | bool | *(Optional.)* When true, per-iteration PGM frames were not written (metrics from memory); final iteration PGM may still exist for baselines. |
| `skip_compression` | bool | *(Optional.)* When true (Java **`--skip-compression`** or **`-sc`**), the encode phase was not run; codebook was loaded from disk; **`compression_seconds`** is **0**. |
| `skip_decompression` | bool | *(Optional.)* When true (Java **`--skip-decompression`** or **`-sd`**), decode was not run; **`decompression_*`** timings are **0**; **`mse_final`** / **`mae_final`** / **`psnr_final`** may be **`null`**; **`png_bytes_final_iter`** may be **0**. |
| `compression_parallelism` | int | Thread pool size for encode / codebook search (Java **`-p`**). |
| `decompression_parallelism` | int | Thread pool size for decode passes (Java **`-P`**, or **`-p`** when **`-P`** omitted). |
| `compression_seconds` | number | Encode / codebook build time. |
| `decompression_seconds` | number | Decode phase time. |
| `decompression_decode_seconds` | number | *(Optional.)* Cumulative wall time for parallel fractal decode only (`latch.await` work per iteration). |
| `decompression_save_seconds` | number | *(Optional.)* Cumulative wall time for **`PGMAUtils.saveToImage`** (initial random frame plus each iteration snapshot). |
| `decompression_snapshot_metrics_seconds` | number | *(Optional.)* Cumulative wall time after each snapshot: error metrics (e.g. loading PGMs / comparison) plus **`MemorySampler.sample()`**. |
| `decode_iteration_avg_seconds` | number | *(Optional.)* Mean wall time per decompression pass (one **`latch.await`** span). |
| `decode_iteration_min_seconds` | number | *(Optional.)* Shortest single-pass decode time. |
| `decode_iteration_max_seconds` | number | *(Optional.)* Longest single-pass decode time. |
| `bytes_original_input` | int | Primary input file size (bytes). |
| `bytes_original_pgm_copy` | int | Copy kept next to artifacts, if applicable. |
| `bytes_codebook` | int | Fractal artifact size (bytes). |
| `png_bytes_original_pgm` | int | Lossless PNG size baseline for original raster (see Java `CompressionBaselines`). |
| `png_bytes_final_iter` | int | PNG size baseline for final reconstruction. |
| `zip_bytes_original_pgm` | int | Single-entry DEFLATE zip of original bytes. |
| `zip_bytes_codebook` | int | Single-entry DEFLATE zip of codebook bytes. |
| `ratio_original_over_codebook` | number | `bytes_original_input / bytes_codebook`. |
| `mse_final` | number | Final MSE vs reference. |
| `mae_final` | number | Final MAE vs reference. |
| `psnr_final` | number | Final PSNR (dB); `null` if undefined. |

## JVM / runtime memory samples (Java today; optional for others)

These keys are **optional** for C++/Python if not applicable; use **`null`** or omit only if your tooling agrees (prefer **`null`** or **0** with documented meaning).

| Key | Description |
|-----|-------------|
| `peak_heap_used_bytes` | Peak sampled heap (Java). |
| `peak_runtime_used_bytes` | Peak `totalMemory - freeMemory` (Java). |
| `avg_heap_used_bytes` | Average heap over samples. |
| `avg_runtime_used_bytes` | Average of runtime used over samples. |

Other runtimes may map their own counters here or document alternatives under **`extras`**.

## `runner` object

| Key | Type | Description |
|-----|------|-------------|
| `language` | string | `"java"`, `"cpp"`, `"python"`, … |
| `id` | string | Stable id for the binary/project (e.g. `java-compression`). Override with env **`BENCHMARK_RUNNER_ID`** in Java. |
| `version` | string | Implementation or build id; may be empty. Env **`BENCHMARK_RUNNER_VERSION`** in Java. |

## `host` object (optional, recommended)

Portable hardware/OS snapshot. Present in the Java reference runner; C++/Python should populate when easy (OS APIs, `/proc`, etc.).

Documented keys (see Java `HostEnvironmentMetrics`): `cpu_logical_cores`, `os_name`, `os_version`, `os_arch`, `os_total_memory_bytes`, `os_free_memory_bytes`, `process_cpu_load`, `system_cpu_load`, heap / JVM fields for Java, `jvm_*`, `jvm_input_args`.

Non-JVM runners may leave JVM-specific keys out or place OS/CPU info under **`extras`** until a shared naming extension is agreed.

## `extras` object (optional)

Free-form JSON object for unstandardized metrics (e.g. CUDA device, compiler flags). Must not replace core fields.

## Files

| Path | Role |
|------|------|
| [`registry.jsonl`](registry.jsonl) | Repo-level append-only log. |
| `processes/<stem>/run_manifest.json` | Per-run copy (Java); other languages may mirror paths. |

See also: [`README.md`](README.md) in this folder if present.
