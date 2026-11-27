
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

This produces the aggregate baseline report.

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

1. Executes the original benchmark and retains its aggregate metrics.
2. Executes a separate diagnostic pass.
3. Adds question-level explanations to the JSON report.

The latency figures in the baseline section are measured during the original
benchmark pass. They do not include diagnostic report construction.

## Understanding question diagnostics

Each question has three main sections.

### `answerability`

Shows whether the question was expected to be answerable, whether the
specialist considered it supported, and the resulting classification:

- `TRUE_POSITIVE`
- `FALSE_POSITIVE`
- `FALSE_NEGATIVE`
- `TRUE_NEGATIVE`

A false positive in this section means an unsupported question was treated as
supported.

### `evidence`

Shows expected and returned evidence, together with:

- `true_positive`: returned evidence that matches the benchmark labels.
- `false_positive`: returned evidence not included in the benchmark labels.
- `false_negative`: expected evidence that was not returned.

The comparison uses exact source ID and sentence matching.

### `matching`

Explains the deterministic token-overlap matcher:

- `query_terms`: normalized terms derived from the research topic and question.
- `matched_terms`: query terms found in an individual source sentence.
- `relevance_score`: matched query terms divided by total query terms.
- `minimum_relevance_score`: the configured acceptance threshold.
- `selection_rank`: candidate ranking based on relevance and stable source order.
- `selection_reason`: why the candidate was selected or not selected.

Possible selection reasons:

- `SELECTED`
- `NO_TERM_OVERLAP`
- `BELOW_MINIMUM_RELEVANCE_SCORE`
- `EXCLUDED_BY_EVIDENCE_LIMIT`

`selection_consistency_errors` checks whether the diagnostic's reconstructed
selection matches the evidence actually returned by the graph.

## Important limitations

The diagnostic explains the current deterministic matching algorithm. It does
not determine whether an evidence sentence semantically supports a question.

The benchmark labels are manually authored and the source material is
synthetic. These diagnostics must not be interpreted as a factuality score.

Do not change the benchmark labels merely to improve the reported metrics.
Changes to expected evidence require an explicit review of the benchmark
source material and annotation.
