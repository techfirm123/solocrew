# SC-WORKFORCE-001-B1.4.2 — Deterministic Question Intent and Relationship Matching

## Objective

Improve deterministic evidence selection by distinguishing subject overlap
from evidence that addresses the outcome requested by a question.

This increment follows B1.4.1 controlled vocabulary normalization.

## Problem observed

The B1.4.1 challenge benchmark retained two false positives:

1. A question about whether RAM improves battery life selected a sentence
   describing RAM and application multitasking.
2. A question about whether CPU replacement improves battery life selected
   a sentence describing CPU socket compatibility.

Both candidates shared question terms but did not address the requested outcome.

## Implementation

A bounded intent module recognizes selected yes/no question patterns.

It extracts:

- Subject terms.
- An impact predicate.
- Outcome terms.

For recognized patterns, a candidate sentence must contain at least one
subject term when subject terms exist and every extracted outcome term.

The existing normalized term matching, minimum matching count, relevance
threshold, stable ranking and evidence limit remain in place.

Evaluation diagnostics expose:

- Extracted question intent.
- Candidate subject-term coverage.
- Candidate outcome-term coverage.
- Missing terms.
- Intent rejection reason.
- Selected evidence and graph-selection consistency.

## Revision identifiers

- Graph: `deterministic-question-focused-v4`
- Intent: `deterministic-outcome-intent-v1`
- Normalization: `controlled-concept-aliases-v1`

## Validation results

Local validation on Python 3.14.7:

- 44 Python tests passed.
- Ruff lint passed.
- Ruff formatting passed.
- Controlled benchmark passed.
- Challenge benchmark passed.

Challenge benchmark:

- Questions: 5.
- Evidence true positives: 3.
- Evidence false positives: 0.
- Evidence false negatives: 0.
- Answerability true positives: 2.
- Answerability true negatives: 3.
- Answerability false positives: 0.
- Answerability false negatives: 0.
- Status consistency errors: 0.
- Selection consistency errors: 0.

All five challenge scenarios were classified as expected.

Controlled benchmark:

- Questions: 13.
- Evidence true positives: 10.
- Evidence false positives: 0.
- Evidence false negatives: 0.
- Evidence precision: 1.0.
- Evidence recall: 1.0.
- Evidence F1: 1.0.
- Answerability accuracy: 1.0.
- Citation traceability: 10/10.
- Status consistency errors: 0.

## Limitations

The intent extractor is deliberately narrow.

It does not provide general semantic parsing, synonym inference,
comprehensive negation understanding, or unrestricted natural-language
reasoning.

Requiring all extracted outcome terms can reject legitimate evidence
expressed using different vocabulary. Such failures should be captured in
future challenge cases before expanding the rules.

## Delivery status

Implemented and locally validated.

Part of the combined SC-WORKFORCE-001-B1.4 feature branch.
Pending final branch review, commit, PR and merge.
