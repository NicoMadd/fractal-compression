# Benchmarks (cross-implementation)

- **[`RUN_RECORD.md`](RUN_RECORD.md)** — JSON/JSONL schema for one benchmark **run** (`schema_version`, `runner`, core metrics, `host`, optional `extras`). Use the same shape from **Java**, **C++**, or **Python** so results can live in one [`registry.jsonl`](registry.jsonl).
- **`registry.jsonl`** — append-only log of run records (one JSON object per line).
- **[`registry-analysis.md`](registry-analysis.md)** — example interpretation (tables, hypotheses); update when your registry snapshot changes.

Java reference tooling: [`../implementations/java/benchmarks/`](../implementations/java/benchmarks/) (`run_matrix.sh`, matrix files).
