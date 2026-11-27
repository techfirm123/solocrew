
# SC-WORKFORCE-001-B1.3 — Research Challenge Benchmark

## Status

Implementation prepared. Local validation is pending.

Branch:

`feature/SC-WORKFORCE-001-B1.3-research-challenge-benchmark`

## Objective

Expand Personal Research Specialist evaluation beyond the original controlled
synthetic benchmark.

The objective is to identify where question-focused deterministic token
matching fails, without changing the matcher in this increment.

## Existing benchmark preserved

`services/specialist-runtime/evaluation/benchmarks/research-v1.json`

This remains the controlled regression benchmark for the current graph
revision:

`deterministic-question-focused-v2`

It contains three scenarios and thirteen questions.

## New challenge benchmark

`services/specialist-runtime/evaluation/benchmarks/research-challenge-v1.json`

The challenge benchmark contains five scenarios:

1. Paraphrased thermal question.
2. Short question with misleading keyword overlap.
3. High-overlap distractor.
4. Multi-sentence battery evidence.
5. Unsupported screen/SSD question.

All source material is synthetic.

## Expected challenge baseline

The current deterministic matcher is expected to expose these limitations:

- One false negative caused by vocabulary mismatch.
- Two false positives caused by misleading keyword overlap.
- One correctly retrieved question requiring two evidence sentences.
- One unsupported question correctly rejected.

Expected aggregate counts:

| Metric | Expected |
|---|---:|
| Evidence true positives | 2 |
| Evidence false positives | 2 |
| Evidence false negatives | 1 |
| Answerability true positives | 1 |
| Answerability false positives | 2 |
| Answerability false negatives | 1 |
| Answerability true negatives | 1 |
| Questions evaluated | 5 |

These expected values describe the current graph's challenge baseline, not
quality targets.

## Algorithm changes

None.

The research graph remains:

`deterministic-question-focused-v2`

The evidence threshold, minimum term count, question-only matching and
maximum evidence count remain unchanged.

## Files created

- `services/specialist-runtime/evaluation/benchmarks/research-challenge-v1.json`
- `services/specialist-runtime/tests/test_research_challenge_benchmark.py`
- `docs/handoffs/SC-WORKFORCE-001-B1.3-research-challenge-benchmark.md`

## Files updated

- `services/specialist-runtime/evaluation/README.md`

## Validation

From `services/specialist-runtime`:

```bash
uv run ruff check . --fix && uv run ruff format . && uv run ruff check . && uv run ruff format --check . && uv run pytest -v
```

Run the existing controlled benchmark:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
```

Run the challenge benchmark and diagnostics:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics \
  --benchmark evaluation/benchmarks/research-challenge-v1.json
```

Save the complete challenge report:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics \
  --benchmark evaluation/benchmarks/research-challenge-v1.json \
  > /tmp/solocrew-research-challenge-v1.json
```

The challenge report must retain zero status-consistency and selection-
consistency errors.

The known false positives and false negative are intentional observations
about the current matcher. They are not test failures.

## Interpretation and limitations

The challenge benchmark does not establish factual correctness or real-world
research quality.

The multi-sentence case tests evidence retrieval, not natural-language
synthesis or reasoning across evidence.

The current benchmark set remains small and synthetic. It must be expanded
before it can be used as a general quality gate.

## Next step

Review the challenge output and decide which limitations are best addressed
with deterministic improvements.

Do not introduce an LLM merely to improve the benchmark score. First establish
whether synonym handling, question normalization, evidence grouping or another
deterministic capability can address the observed gaps.
