# ADR-0004: Deterministic Specialist Evaluation Before AI Enhancement

- Status: Accepted
- Date: 2026-11-27
- Scope: All SoloCrew digital specialists

## Context

SoloCrew is building responsibility-bearing digital employees.

A specialist must be evaluated by how well it fulfils its
responsibilities, not simply by whether its code executes or
whether an LLM produces a plausible response.

AI can increase capability, but it can also introduce:

- Inconsistent results.
- Unsupported claims.
- Additional latency.
- Inference cost.
- Provider availability dependencies.
- More difficult failure diagnosis.

We need a measurable baseline before introducing AI.

## Decision

### Deterministic capability comes first

Each specialist must first implement its core responsibilities
using conventional software, explicit rules, algorithms and
controlled integrations.

A specialist must remain useful when no LLM provider is configured
or available.

### Establish a benchmark before optimisation

Each specialist must have a versioned evaluation dataset covering
representative responsibilities and known edge cases.

The benchmark should include:

- Answerable and unanswerable scenarios.
- Expected outcomes.
- Traceable evidence or source records where applicable.
- Negative and boundary cases.
- Manually reviewed expected results.

Benchmark versions used as baselines must not be silently changed.

Changes to expected results require review and documentation.

### Measure before improving

Metrics must reflect the specialist's responsibility.

The Personal Research Specialist initially measures:

- Evidence precision.
- Evidence recall.
- Evidence F1.
- Question answerability accuracy.
- Answerable question recall.
- Unsupported question rejection rate.
- False support rate.
- Citation traceability.
- Execution latency.

Additional metrics will be introduced as source discovery,
retrieval and reporting capabilities are implemented.

### AI must demonstrate measurable value

An AI enhancement must be compared with the deterministic
baseline using the same benchmark.

The evaluation must consider:

- Quality improvement.
- Regression in existing scenarios.
- Execution latency.
- Reliability and failure modes.
- Inference usage and cost.
- Deterministic fallback behaviour.

AI must not be introduced merely because a capability can be
implemented using an LLM.

### Evaluation limitations must be explicit

A benchmark measures only what its dataset and metrics represent.

Synthetic source excerpts can measure extraction, ranking and
traceability behaviour. They cannot establish the factual
correctness or authority of those excerpts.

Evaluation reports must disclose unmeasured dimensions.

### Quality thresholds are evidence-driven

Do not establish arbitrary quality thresholds before observing
the initial deterministic baseline.

Set improvement targets after reviewing benchmark results
and understanding the specialist's actual limitations.

## Consequences

- Specialist development begins with deterministic capabilities.
- Evaluation datasets are versioned and stored with the codebase.
- Every meaningful algorithm change can be compared against
  the same benchmark.
- AI enhancements require evidence of improvement.
- Local and hosted model providers remain optional.
- Inference cost and latency are part of the evaluation.
- Specialists can improve without introducing AI dependencies.

## Initial implementation

The Personal Research Specialist establishes the first evaluation
framework through:

    services/specialist-runtime/evaluation/

The initial benchmark is:

    evaluation/benchmarks/research-v1.json

Its source excerpts are synthetic controlled fixtures and are
not authoritative technical references.

## Revisit this decision when

- A specialist responsibility requires a different evaluation
  methodology.
- A benchmark becomes unrepresentative of actual business work.
- Business validation provides evidence that new outcome metrics
  are required.
