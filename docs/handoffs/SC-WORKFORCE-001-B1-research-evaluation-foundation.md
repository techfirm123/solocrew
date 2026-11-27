# SC-WORKFORCE-001-B1 — Research Evaluation Foundation

- Status: Implementation handoff prepared; local validation pending
- Date: 2026-11-27
- Branch: feature/SC-WORKFORCE-001-B1-research-evaluation-foundation

## Objective

Establish a reproducible evaluation foundation for the deterministic
Personal Research Specialist.

The goal is to measure the current graph before improving its
research capabilities or introducing AI.

## Product principle

Build deterministic capability first.

Measure its performance.

Identify actual limitations.

Introduce AI only where evaluation demonstrates meaningful value.

## Included

- Versioned research benchmark definition.
- Three controlled research scenarios.
- Answerable and deliberately unanswerable questions.
- Manually labelled expected evidence.
- Benchmark validation.
- Evidence precision, recall and F1.
- Question answerability metrics.
- Unsupported-question rejection metrics.
- Citation traceability.
- Execution latency measurements.
- JSON evaluation report.
- Pytest coverage for evaluation logic.
- Python 3.13 and 3.14 CI validation.

## Initial benchmark

    services/specialist-runtime/evaluation/benchmarks/research-v1.json

Benchmark ID:

    solocrew-personal-research

Version:

    1.0.0

Graph revision:

    deterministic-token-overlap-v1

Cases:

1. Laptop battery health.
2. Laptop overheating.
3. SSD reliability.

Total questions: 13.

Source material is synthetic and controlled. It is intended to
measure algorithm behaviour, not to establish factual correctness
or external source authority.

## Run evaluation

From `services/specialist-runtime`:

    uv run python -m solocrew_runtime.evaluation.run_benchmark

To save a report:

    uv run python -m solocrew_runtime.evaluation.run_benchmark \
      --output /tmp/solocrew-research-v1-report.json

## Validation

    uv sync --locked --group dev
    uv run pytest -v
    uv run ruff check .
    uv run ruff format --check .

The benchmark must also execute successfully through the CLI.

## Acceptance criteria

- [ ] Benchmark JSON validates.
- [ ] All expected evidence references an existing source.
- [ ] Expected evidence matches an exact source sentence.
- [ ] All benchmark scenarios execute through the existing LangGraph.
- [ ] Evidence metrics are calculated correctly.
- [ ] Question answerability metrics are calculated correctly.
- [ ] Citation traceability is measured.
- [ ] Execution latency is reported.
- [ ] Evaluation results are reproducible apart from latency.
- [ ] All Python tests pass.
- [ ] Ruff lint passes.
- [ ] Ruff formatting passes.
- [ ] CI passes on Python 3.13 and 3.14.
- [ ] Root README and project handoff are updated.

## Quality thresholds

No pass/fail research-quality threshold is introduced in this slice.

The first evaluation run establishes the baseline. Improvement
targets will be decided after reviewing the actual results.

## Deliberately excluded

- Java integration.
- Temporal assignment workflows.
- External web search.
- Source authority assessment.
- Full-document retrieval.
- Ollama.
- Other LLM providers.
- AI-generated research synthesis.
- Owner-facing research assignment workflow.

## Next slice

SC-WORKFORCE-001-B2 — Deterministic Source Discovery and Retrieval.

The next slice will introduce source discovery and retrieval
capabilities, then evaluate their contribution against this
baseline and an appropriately expanded benchmark.
