
# SC-WORKFORCE-001-B1.2 — Question-Focused Deterministic Matching

## Status

Implementation prepared. Local validation is pending.

Branch:

`feature/SC-WORKFORCE-001-B1-research-evaluation-foundation`

## Objective

Improve deterministic evidence precision and unsupported-question rejection
using the failures identified by SC-WORKFORCE-001-B1.1 diagnostics.

## Problem identified

The original graph constructed matching terms from both the research topic
and the research question.

This caused broad topic terms to count as evidence of question relevance.

For example, an unrelated battery-storage sentence matched the topic terms
`laptop` and `battery`. It was therefore selected for questions about battery
age, operating conditions, swollen batteries and RAM replacement.

The RAM replacement question was incorrectly marked as supported even though
the selected evidence did not discuss RAM.

A separate SSD false positive occurred because a sentence containing the
terms `data` and `protect` was selected for a question about backups and drive
failure.

## Matching changes

The graph now uses question-focused deterministic matching.

Two conditions must both be satisfied:

1. At least two distinct terms from the research question must match.
2. The matched-term proportion must be at least 0.30.

The research topic remains part of the execution request and report title,
but it does not contribute to the evidence relevance score.

Candidate ranking remains deterministic:

- Higher relevance scores rank first.
- Source order breaks score ties.
- Sentence order breaks remaining ties.
- A maximum of four evidence sentences is returned per question.

## Revision identifier

Previous graph revision:

`deterministic-token-overlap-v1`

New graph revision:

`deterministic-question-focused-v2`

The benchmark source material and expected evidence labels remain unchanged.

## Experimental comparison

Original deterministic baseline:

| Metric | Original |
|---|---:|
| Evidence true positives | 10 |
| Evidence false positives | 5 |
| Evidence false negatives | 0 |
| Evidence precision | 66.67% |
| Evidence recall | 100% |
| Answerability true positives | 10 |
| Answerability false positives | 1 |
| Answerability true negatives | 2 |
| Answerability accuracy | 92.31% |
| Unsupported question rejection | 66.67% |

Expected refined result against the current synthetic benchmark:

| Metric | Expected refined result |
|---|---:|
| Evidence true positives | 10 |
| Evidence false positives | 0 |
| Evidence false negatives | 0 |
| Evidence precision | 100% |
| Evidence recall | 100% |
| Answerability true positives | 10 |
| Answerability false positives | 0 |
| Answerability true negatives | 3 |
| Answerability accuracy | 100% |
| Unsupported question rejection | 100% |

These refined values are expected results from the matching experiment. They
must be confirmed by running the implementation locally.

A perfect score on this small synthetic benchmark is not evidence of general
research accuracy.

## Files changed

- `services/specialist-runtime/src/solocrew_runtime/research/graph.py`
- `services/specialist-runtime/src/solocrew_runtime/evaluation/diagnostics.py`
- `services/specialist-runtime/tests/test_research_graph.py`
- `services/specialist-runtime/tests/test_evaluation_diagnostics.py`
- `services/specialist-runtime/evaluation/benchmarks/research-v1.json`
- `services/specialist-runtime/evaluation/README.md`

## Validation

From `services/specialist-runtime`:

```bash
uv run ruff check .
uv run ruff format .
uv run ruff check .
uv run ruff format --check .
uv run pytest -v
uv run python -m solocrew_runtime.evaluation.run_benchmark
uv run python -m solocrew_runtime.evaluation.run_diagnostics
```

The diagnostic run must report:

- 13 questions evaluated.
- Zero status consistency errors.
- Zero selection consistency errors.
- No false-positive evidence against the current benchmark labels.
- No false-negative evidence against the current benchmark labels.

## Known limitations

- Only three synthetic scenarios are currently evaluated.
- Question wording is relatively explicit.
- Short, ambiguous and paraphrased questions are underrepresented.
- Two matching terms and a 0.30 threshold are initial heuristics.
- Exact evidence matching does not establish semantic support.
- No real external sources or source discovery are evaluated.
- No LLM is used.

## Next step

Expand the research benchmark before introducing AI.

Add cases for:

- Short and ambiguous questions.
- Paraphrased questions.
- Questions that use synonyms absent from the source text.
- Questions where several sentences share generic terms.
- Questions that require combining evidence from multiple sentences.
- Unsupported questions with strong but misleading keyword overlap.

The next evaluation should determine whether the current matching rules generalize
or whether they need further deterministic improvements.

AI should be evaluated only after this larger deterministic benchmark is
available.
