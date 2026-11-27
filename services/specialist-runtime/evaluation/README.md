
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

It is intentionally retained as a stable reference for the current
question-focused deterministic matcher.

Run it from `services/specialist-runtime`:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
```

## 2. Challenge benchmark

`benchmarks/research-challenge-v1.json` contains five additional scenarios:

- Paraphrased questions with vocabulary differences.
- Short questions with misleading keyword overlap.
- Distractors with strong lexical overlap but insufficient semantic support.
- Questions requiring multiple evidence sentences.
- Unsupported questions used as negative controls.

Run the challenge benchmark with diagnostics:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics \
  --benchmark evaluation/benchmarks/research-challenge-v1.json
```

To save the complete report:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics \
  --benchmark evaluation/benchmarks/research-challenge-v1.json \
  > /tmp/solocrew-research-challenge-v1.json
```

The report contains aggregate benchmark metrics and question-level diagnostics.

## 3. Current deterministic matching rules

The current graph revision is:

`deterministic-question-focused-v2`

Evidence matching uses terms extracted from the research question.

The research topic remains available as context, but its terms do not contribute
to the evidence relevance score.

A source sentence must satisfy both conditions:

- At least two distinct question terms must match.
- The proportion of question terms matched must be at least 0.30.

Eligible sentences are ranked by relevance score. Ties retain stable source
and sentence ordering. A maximum of four evidence sentences is returned per
question.

These are initial deterministic heuristics, not general-purpose semantic
relevance rules.

## 4. Understanding diagnostics

Each question diagnostic has three principal sections.

### `answerability`

Shows whether the question was expected to be answerable, whether the
specialist considered it supported, and the resulting classification:

- `TRUE_POSITIVE`
- `FALSE_POSITIVE`
- `FALSE_NEGATIVE`
- `TRUE_NEGATIVE`

### `evidence`

Shows expected and returned evidence:

- `true_positive`: returned evidence that matches the benchmark labels.
- `false_positive`: returned evidence not included in the benchmark labels.
- `false_negative`: expected evidence that was not returned.

Evidence is compared using exact source ID and sentence matching.

### `matching`

Explains the deterministic matcher:

- `question_terms`: normalized terms used for evidence matching.
- `topic_context_terms`: terms retained for context.
- `matched_terms`: question terms found in a source sentence.
- `relevance_score`: matched question terms divided by total question terms.
- `minimum_matched_terms`: the minimum number of distinct matching terms.
- `minimum_relevance_score`: the configured acceptance threshold.
- `selection_rank`: candidate ranking based on relevance and stable source order.
- `selection_reason`: why the candidate was selected or rejected.

## 5. Interpreting the challenge benchmark

The challenge benchmark is expected to expose weaknesses in the current matcher.

A false negative on a paraphrased question can indicate vocabulary mismatch.
A false positive on a short question can indicate that a small number of
overlapping terms are insufficient to establish semantic support.

The multi-sentence scenario tests whether the graph can retrieve multiple
relevant evidence sentences. It does not establish that the specialist can
synthesize those sentences into a complete answer.

Do not modify the expected evidence labels merely to improve scores.

If the matcher is changed, preserve the challenge benchmark as a historical
comparison and review any new expected outcomes explicitly.

## 6. Limitations

Both benchmarks use synthetic material.

They do not establish:

- Factual correctness of the authored source material.
- Authority, freshness or diversity of real external sources.
- Research discovery recall beyond supplied source material.
- Semantic equivalence across arbitrary paraphrases.
- Correct synthesis of multi-source evidence.
- Quality of natural-language recommendations.
- Business usefulness of the final research report.

No LLM is used by either benchmark.
