
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

It is retained as a stable reference dataset. Its graph revision identifies
the implementation used for the current run.

Run it from `services/specialist-runtime`:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
```

## 2. Challenge benchmark

`benchmarks/research-challenge-v1.json` contains five scenarios:

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

## 3. Current graph and normalization revisions

Current graph revision:

`deterministic-question-focused-v3`

Current normalization revision:

`controlled-concept-aliases-v1`

Evidence matching uses terms extracted from the research question.

The research topic remains available as context, but its terms do not contribute
to the evidence relevance score.

The controlled normalizer applies:

- Existing simple plural normalization.
- Explicit aliases for selected thermal-condition vocabulary.
- Explicit aliases for selected causal-relation vocabulary.

The current concept groups are:

| Concept group | Controlled aliases |
|---|---|
| `temperature_condition` | hot, hotter, overheat, overheated, overheating, temperature |
| `causal_relation` | cause, causing, increase, increasing, make, made, raise, raised |

Generic `heat` and `thermal` remain independent terms. They are deliberately
not collapsed into `temperature_condition`, because doing so can create
unrelated matches in technical contexts such as thermal paste and heat transfer.

These groups are small, reviewed matching aids. They do not establish
unrestricted semantic equivalence.

The matching thresholds remain:

- At least two distinct normalized question terms must match.
- The proportion of normalized question terms matched must be at least 0.30.
- A maximum of four evidence sentences is returned per question.

Eligible sentences are ranked by relevance score. Ties retain stable source
and sentence ordering.

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

- `normalization_revision`: controlled vocabulary mapping revision.
- `question_term_normalization`: original terms and their normalized forms.
- `topic_term_normalization`: normalized topic terms.
- `question_terms`: canonical terms used for evidence matching.
- `topic_context_terms`: terms retained for context.
- `matched_terms`: normalized question terms found in a source sentence.
- `normalization_trace`: token-level normalization of each candidate sentence.
- `relevance_score`: matched question terms divided by total question terms.
- `minimum_matched_terms`: the minimum number of distinct matching terms.
- `minimum_relevance_score`: the configured acceptance threshold.
- `selection_rank`: candidate ranking based on relevance and stable source order.
- `selection_reason`: why the candidate was selected or rejected.

The original source sentence remains unchanged in returned evidence.

## 5. Interpreting the challenge benchmark

The challenge benchmark is expected to expose weaknesses in the current matcher.

Controlled concept normalization is intended to improve selected vocabulary
mismatches. It does not solve:

- Arbitrary paraphrases.
- Synonyms that have not been explicitly mapped.
- Entity and action relationship understanding.
- Questions where matching words appear in unrelated contexts.
- Natural-language synthesis across multiple evidence items.

The multi-sentence scenario tests evidence retrieval, not whether the specialist
can synthesize the evidence into a complete answer.

Do not modify expected evidence labels merely to improve scores.

If the matcher is changed, review the new outcomes against both benchmark
suites and preserve historical results.

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
