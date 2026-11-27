# Personal Research Specialist Evaluation

This directory contains controlled benchmarks for the deterministic Personal
Research Specialist.

There are two benchmark suites:

- `benchmarks/research-v1.json`: controlled regression benchmark.
- `benchmarks/research-challenge-v1.json`: challenge benchmark designed to
  expose limitations in deterministic matching.

## 1. Controlled regression benchmark

`benchmarks/research-v1.json` contains synthetic source material covering:

- Laptop battery health
- Laptop overheating
- SSD reliability

The benchmark checks evidence extraction, question answerability and citation
traceability.

It is intentionally retained as a stable regression reference while the
deterministic matcher evolves.

Run it from `services/specialist-runtime`:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
