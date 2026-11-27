
# Personal Research Specialist Evaluation

This directory contains controlled benchmarks for the deterministic Personal
Research Specialist.

## Benchmark

`benchmarks/research-v1.json` contains synthetic source material covering:

- Laptop battery health
- Laptop overheating
- SSD reliability

The benchmark checks evidence extraction, question answerability and citation
traceability. It does not establish factual correctness, external-source
quality, or business usefulness.

## Run the original benchmark

From `services/specialist-runtime`:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
```

This produces the aggregate benchmark report.

## Run the benchmark with diagnostics

From `services/specialist-runtime`:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics
```

An alternate benchmark file can be supplied:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics \
  --benchmark evaluation/benchmarks/research-v1.json
```

The diagnostic runner:

1. Executes the aggregate benchmark.
2. Executes a separate diagnostic pass.
3. Adds question-level explanations to the JSON report.

The latency figures in the baseline section are measured during the original
benchmark pass. They do not include diagnostic report construction.

## Current deterministic matching rules

The current graph revision is:

`deterministic-question-focused-v2`

Evidence matching uses the terms extracted from the research question.

The research topic remains available as context, but its terms do not contribute
to the evidence relevance score.

A source sentence must satisfy both conditions:

- At least two distinct question terms must match.
- The proportion of question terms matched must be at least 0.30.

Eligible sentences are ranked by relevance score. Ties retain stable source
and sentence ordering. A maximum of four evidence sentences is returned per
question.

These are initial deterministic heuristics. They must be evaluated against a
broader benchmark before being treated as general-purpose relevance rules.

## Understanding question diagnostics

Each question has three main sections.

### `answerability`

Shows whether the question was expected to be answerable, whether the
specialist considered it supported, and the resulting classification:

- `TRUE_POSITIVE`
- `FALSE_POSITIVE`
- `FALSE_NEGATIVE`
- `TRUE_NEGATIVE`

A false positive means an unsupported question was treated as supported.

### `evidence`

Shows expected and returned evidence, together with:

- `true_positive`: returned evidence that matches the benchmark labels.
- `false_positive`: returned evidence not included in the benchmark labels.
- `false_negative`: expected evidence that was not returned.

The comparison uses exact source ID and sentence matching.

### `matching`

Explains the deterministic matcher:

- `question_terms`: normalized terms used for evidence matching.
- `topic_context_terms`: terms retained for context, not evidence scoring.
- `matched_terms`: question terms found in an individual source sentence.
- `relevance_score`: matched question terms divided by total question terms.
- `minimum_matched_terms`: the minimum number of distinct matching terms.
- `minimum_relevance_score`: the configured acceptance threshold.
- `selection_rank`: candidate ranking based on relevance and stable source order.
- `selection_reason`: why the candidate was selected or rejected.

Possible selection reasons:

- `SELECTED`
- `NO_QUESTION_TERM_OVERLAP`
- `BELOW_MINIMUM_MATCHED_TERMS`
- `BELOW_MINIMUM_RELEVANCE_SCORE`
- `EXCLUDED_BY_EVIDENCE_LIMIT`

`selection_consistency_errors` checks whether the diagnostic's reconstructed
selection matches the evidence actually returned by the graph.

## Benchmark interpretation

The current benchmark contains only three synthetic scenarios and thirteen
questions. A perfect score on it does not establish general research quality.

The benchmark must be expanded with varied question formulations, ambiguous
questions, short questions, distractor evidence and additional unsupported
questions.

Do not change expected evidence labels merely to improve reported metrics.
Changes to expected evidence require an explicit review of the source material
and annotations.
