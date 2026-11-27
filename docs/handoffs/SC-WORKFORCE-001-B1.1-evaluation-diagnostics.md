
# SC-WORKFORCE-001-B1.1 — Evaluation Diagnostics

## Status

Implementation prepared. Local validation is pending.

Branch:

`feature/SC-WORKFORCE-001-B1-research-evaluation-foundation`

## Objective

Make the Personal Research Specialist's deterministic benchmark results
explainable at the individual-question and individual-evidence level.

This increment is diagnostic-only. It does not intentionally change the
research matching algorithm, the specialist's API contract, or the original
benchmark's aggregate calculations.

## Implemented files

- `services/specialist-runtime/src/solocrew_runtime/evaluation/diagnostics.py`
- `services/specialist-runtime/src/solocrew_runtime/evaluation/run_diagnostics.py`
- `services/specialist-runtime/tests/test_evaluation_diagnostics.py`
- `services/specialist-runtime/evaluation/README.md`

## Diagnostic capabilities

For each benchmark question, the diagnostic report provides:

- Expected and actual answerability.
- Answerability classification.
- Expected and returned evidence.
- True-positive, false-positive and false-negative evidence.
- Per-question precision, recall and F1.
- Normalized query terms.
- Candidate source sentences and matched terms.
- Candidate relevance scores.
- Candidate ranking and selection reasons.
- A consistency check comparing diagnostic selection with actual graph output.

The diagnostic uses the research graph's existing normalization, sentence
splitting, relevance threshold and evidence limit.

## Baseline preservation

The original benchmark runner and its calculations remain unchanged.

The diagnostic runner executes:

1. The original aggregate benchmark pass.
2. A separate pass to collect question-level details.

The latency reported in the original benchmark section belongs to the first
pass and excludes diagnostic report construction.

The existing baseline from the initial run was:

- Evidence true positives: 10
- Evidence false positives: 5
- Evidence false negatives: 0
- Answerability true positives: 10
- Answerability false positives: 1
- Answerability false negatives: 0
- Answerability true negatives: 2
- Citation traceability: 100%

These values are regression expectations for the current benchmark and graph
revision, not universal quality targets.

## Local validation

From `services/specialist-runtime`:

```bash
uv run ruff check .
uv run ruff format --check .
uv run pytest -v
uv run python -m solocrew_runtime.evaluation.run_benchmark
uv run python -m solocrew_runtime.evaluation.run_diagnostics
```

The implementation must not be merged until the local checks pass and the
diagnostic selection-consistency checks report zero errors.

## Known limitations

- The benchmark uses synthetic source material.
- Evidence comparison uses exact source ID and sentence matching.
- Token overlap is not semantic relevance.
- Citation traceability does not establish factual correctness.
- No external research discovery or source verification is measured.
- No LLM is used.

## Next increment

Review the diagnostic output, especially the false-positive evidence in the
laptop battery health case and the unsupported question incorrectly accepted
by the specialist.

Only after reviewing those examples should we change the deterministic
matching rules.

The original benchmark must remain available as the regression baseline.
