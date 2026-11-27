
# SC-WORKFORCE-001-B1.4.1 — Controlled Vocabulary Normalization

## Status

Implementation prepared. Local validation is pending.

Branch:

`feature/SC-WORKFORCE-001-B1.4-deterministic-matching-improvements`

## Objective

Improve deterministic matching for a narrowly defined set of vocabulary
variations without introducing an LLM, external dependency, fuzzy matching
or changes to the evidence relevance thresholds.

## Problem addressed

The B1.3 challenge benchmark identified a false negative:

Question:

"What makes a laptop run hot?"

Source:

"High processor load raises internal operating temperature."

The original question-focused token matcher did not recognize the relationship
between the words used in the question and those used in the source.

## Implementation

A separate normalization module was introduced:

`services/specialist-runtime/src/solocrew_runtime/research/normalization.py`

It provides:

- Existing basic plural normalization.
- Explicit controlled concept groups.
- Token-level normalization diagnostics.

### Concept groups

`temperature_condition`:

- hot
- hotter
- overheat
- overheated
- overheating
- temperature

`causal_relation`:

- cause
- causing
- increase
- increasing
- make
- made
- raise
- raised

Generic `heat` and `thermal` remain independent terms. This avoids grouping
words with different technical uses into one broad concept and creating
false-positive evidence.

### Graph revision

Previous:

`deterministic-question-focused-v2`

New:

`deterministic-question-focused-v3`

Normalization revision:

`controlled-concept-aliases-v1`

## Matching behaviour

Unchanged:

- Question-focused matching.
- Minimum two matched terms.
- Minimum relevance score of 0.30.
- Maximum four evidence sentences.
- Stable score, source and sentence ordering.

Changed:

- Query and source tokens use the shared controlled normalizer before matching.
- Returned evidence preserves the exact original source sentence.
- Diagnostics record normalization at question, topic and candidate level.

## Expected benchmark comparison

### Controlled benchmark

The original 13-question benchmark should retain its existing results:

- Evidence precision: 100%.
- Evidence recall: 100%.
- Answerability accuracy: 100%.
- Unsupported-question rejection: 100%.
- Citation traceability: 100%.

### Challenge benchmark

The previous B1.3 challenge result was:

| Metric | Before normalization |
|---|---:|
| Evidence true positives | 2 |
| Evidence false positives | 2 |
| Evidence false negatives | 1 |
| Evidence precision | 50% |
| Evidence recall | 66.67% |
| Answerability accuracy | 40% |

Expected result after controlled vocabulary normalization:

| Metric | Expected |
|---|---:|
| Evidence true positives | 3 |
| Evidence false positives | 2 |
| Evidence false negatives | 0 |
| Evidence precision | 60% |
| Evidence recall | 100% |
| Answerability true positives | 2 |
| Answerability false positives | 2 |
| Answerability false negatives | 0 |
| Answerability true negatives | 1 |
| Answerability accuracy | 60% |

The paraphrased thermal question should now be supported.

The two misleading keyword-overlap scenarios are intentionally expected to
remain false positives. Relationship matching is a separate future increment.

These values are expected outcomes and must be confirmed locally.

## Files created

- `services/specialist-runtime/src/solocrew_runtime/research/normalization.py`
- `services/specialist-runtime/tests/test_research_normalization.py`
- `docs/handoffs/SC-WORKFORCE-001-B1.4.1-controlled-vocabulary-normalization.md`

## Files updated

- `services/specialist-runtime/src/solocrew_runtime/research/graph.py`
- `services/specialist-runtime/src/solocrew_runtime/evaluation/diagnostics.py`
- `services/specialist-runtime/evaluation/benchmarks/research-v1.json`
- `services/specialist-runtime/evaluation/benchmarks/research-challenge-v1.json`
- `services/specialist-runtime/tests/test_evaluation.py`
- `services/specialist-runtime/tests/test_research_challenge_benchmark.py`
- `services/specialist-runtime/evaluation/README.md`

## Validation

From `services/specialist-runtime`:

```bash
uv run ruff check . --fix && uv run ruff format . && uv run ruff check . && uv run ruff format --check . && uv run pytest -v
```

Run the controlled benchmark:

```bash
uv run python -m solocrew_runtime.evaluation.run_benchmark
```

Run challenge diagnostics:

```bash
uv run python -m solocrew_runtime.evaluation.run_diagnostics \
  --benchmark evaluation/benchmarks/research-challenge-v1.json
```

Expected challenge diagnostics:

- Five questions evaluated.
- Three evidence true positives.
- Two evidence false positives.
- Zero evidence false negatives.
- Zero status consistency errors.
- Zero selection consistency errors.

## Limitations

- Concept mappings are manually maintained.
- They do not provide general semantic understanding.
- The mapped words can have different meanings in other contexts.
- The two relationship/distractor false positives remain unresolved.
- No real external research sources are tested.
- No LLM is used.

## Next step

Evaluate the two remaining false-positive scenarios.

Investigate deterministic question-intent and entity-relationship matching,
without changing the current controlled vocabulary groups unless a benchmark
failure provides evidence for doing so.
