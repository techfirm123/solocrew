from __future__ import annotations

import json
import re
import statistics
from pathlib import Path
from time import perf_counter
from typing import Any
from uuid import uuid4

from pydantic import BaseModel, ConfigDict, Field, model_validator

from solocrew_runtime.contracts.research import (
    ResearchExecutionRequest,
    ResearchSourceInput,
)
from solocrew_runtime.research.graph import execute_research_request

EvidenceKey = tuple[str, str]


def split_source_sentences(value: str) -> list[str]:
    """Split a supplied excerpt using the research graph's sentence boundaries."""

    return [
        sentence.strip()
        for sentence in re.split(r"(?<=[.!?])\s+|\n+", value.strip())
        if sentence.strip()
    ]


class ExpectedEvidence(BaseModel):
    """A manually labelled evidence sentence in the benchmark."""

    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    source_id: str = Field(min_length=1, max_length=80)
    excerpt: str = Field(min_length=1, max_length=20_000)


class BenchmarkQuestion(BaseModel):
    """A research question and its manually labelled expected evidence."""

    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    question: str = Field(min_length=5, max_length=500)
    expected_evidence: list[ExpectedEvidence] = Field(default_factory=list)


class BenchmarkCase(BaseModel):
    """One controlled research scenario."""

    model_config = ConfigDict(extra="forbid")

    case_id: str = Field(min_length=1, max_length=80)
    title: str = Field(min_length=1, max_length=200)
    topic: str = Field(min_length=5, max_length=250)
    questions: list[BenchmarkQuestion] = Field(min_length=1, max_length=20)
    sources: list[ResearchSourceInput] = Field(min_length=1, max_length=20)

    @model_validator(mode="after")
    def validate_case(self) -> BenchmarkCase:
        question_keys = [question.question.casefold() for question in self.questions]

        if len(question_keys) != len(set(question_keys)):
            raise ValueError(f"Benchmark case '{self.case_id}' contains duplicate questions.")

        source_ids = [source.source_id for source in self.sources]

        if len(source_ids) != len(set(source_ids)):
            raise ValueError(f"Benchmark case '{self.case_id}' contains duplicate source IDs.")

        source_sentences = {
            source.source_id: set(split_source_sentences(source.excerpt)) for source in self.sources
        }

        for question in self.questions:
            for expected in question.expected_evidence:
                if expected.source_id not in source_sentences:
                    raise ValueError(
                        f"Expected evidence references unknown source "
                        f"'{expected.source_id}' in case '{self.case_id}'."
                    )

                if expected.excerpt not in source_sentences[expected.source_id]:
                    raise ValueError(
                        f"Expected evidence must match an exact sentence in "
                        f"source '{expected.source_id}' for case '{self.case_id}'."
                    )

        return self


class ResearchBenchmark(BaseModel):
    """Versioned benchmark definition."""

    model_config = ConfigDict(extra="forbid")

    benchmark_id: str = Field(min_length=1, max_length=100)
    version: str = Field(min_length=1, max_length=30)
    graph_revision: str = Field(min_length=1, max_length=100)
    name: str = Field(min_length=1, max_length=200)
    source_material: str = Field(min_length=1, max_length=50)
    description: str = Field(min_length=1, max_length=1000)
    cases: list[BenchmarkCase] = Field(min_length=1)

    @model_validator(mode="after")
    def validate_case_ids(self) -> ResearchBenchmark:
        case_ids = [case.case_id for case in self.cases]

        if len(case_ids) != len(set(case_ids)):
            raise ValueError("Benchmark case IDs must be unique.")

        return self


def load_benchmark(path: Path) -> ResearchBenchmark:
    """Load and validate a benchmark JSON file."""

    payload = json.loads(path.read_text(encoding="utf-8"))

    return ResearchBenchmark.model_validate(payload)


def _ratio(numerator: int, denominator: int) -> float | None:
    if denominator == 0:
        return None

    return round(numerator / denominator, 4)


def _new_counts() -> dict[str, int]:
    return {
        "true_positive": 0,
        "false_positive": 0,
        "false_negative": 0,
        "true_negative": 0,
    }


def _add_counts(
    target: dict[str, int],
    source: dict[str, int],
) -> None:
    for key in target:
        target[key] += source[key]


def _record_binary_result(
    counts: dict[str, int],
    expected_positive: bool,
    predicted_positive: bool,
) -> None:
    if expected_positive and predicted_positive:
        counts["true_positive"] += 1
    elif not expected_positive and predicted_positive:
        counts["false_positive"] += 1
    elif expected_positive and not predicted_positive:
        counts["false_negative"] += 1
    else:
        counts["true_negative"] += 1


def score_evidence(
    expected: set[EvidenceKey],
    predicted: set[EvidenceKey],
) -> dict[str, Any]:
    """Calculate strict evidence precision, recall and F1."""

    true_positive = len(expected.intersection(predicted))
    false_positive = len(predicted.difference(expected))
    false_negative = len(expected.difference(predicted))

    precision = _ratio(
        true_positive,
        true_positive + false_positive,
    )

    recall = _ratio(
        true_positive,
        true_positive + false_negative,
    )

    f1: float | None = None

    if precision is not None and recall is not None:
        denominator = precision + recall

        if denominator > 0:
            f1 = round(
                2 * precision * recall / denominator,
                4,
            )
        else:
            f1 = 0.0

    return {
        "true_positive": true_positive,
        "false_positive": false_positive,
        "false_negative": false_negative,
        "precision": precision,
        "recall": recall,
        "f1": f1,
    }


def _evidence_metrics(counts: dict[str, int]) -> dict[str, Any]:
    result = score_evidence(
        set(),
        set(),
    )

    true_positive = counts["true_positive"]
    false_positive = counts["false_positive"]
    false_negative = counts["false_negative"]

    precision = _ratio(
        true_positive,
        true_positive + false_positive,
    )

    recall = _ratio(
        true_positive,
        true_positive + false_negative,
    )

    f1: float | None = None

    if precision is not None and recall is not None:
        denominator = precision + recall

        if denominator > 0:
            f1 = round(
                2 * precision * recall / denominator,
                4,
            )
        else:
            f1 = 0.0

    result.update(
        {
            "true_positive": true_positive,
            "false_positive": false_positive,
            "false_negative": false_negative,
            "precision": precision,
            "recall": recall,
            "f1": f1,
        }
    )

    return result


def _question_metrics(counts: dict[str, int]) -> dict[str, Any]:
    true_positive = counts["true_positive"]
    false_positive = counts["false_positive"]
    false_negative = counts["false_negative"]
    true_negative = counts["true_negative"]

    total = true_positive + false_positive + false_negative + true_negative

    return {
        "total_questions": total,
        "true_positive": true_positive,
        "false_positive": false_positive,
        "false_negative": false_negative,
        "true_negative": true_negative,
        "answerability_accuracy": _ratio(
            true_positive + true_negative,
            total,
        ),
        "answerable_question_recall": _ratio(
            true_positive,
            true_positive + false_negative,
        ),
        "unsupported_question_rejection_rate": _ratio(
            true_negative,
            true_negative + false_positive,
        ),
        "false_support_rate": _ratio(
            false_positive,
            false_positive + true_negative,
        ),
    }


def _citation_metrics(
    valid_count: int,
    invalid_count: int,
) -> dict[str, Any]:
    total = valid_count + invalid_count

    return {
        "valid_evidence_count": valid_count,
        "invalid_evidence_count": invalid_count,
        "total_evidence_count": total,
        "traceability_rate": _ratio(valid_count, total),
    }


def _evaluate_case(case: BenchmarkCase) -> dict[str, Any]:
    """Execute one benchmark case and collect its evaluation results."""

    request = ResearchExecutionRequest(
        assignment_id=uuid4(),
        execution_id=uuid4(),
        topic=case.topic,
        research_questions=[question.question for question in case.questions],
        sources=case.sources,
    )

    started_at = perf_counter()
    response = execute_research_request(request)
    execution_ms = (perf_counter() - started_at) * 1000

    findings_by_question = {finding.question: finding for finding in response.findings}

    expected_questions = {question.question for question in case.questions}

    if set(findings_by_question) != expected_questions:
        raise ValueError(
            f"Research graph returned an unexpected question set "
            f"for benchmark case '{case.case_id}'."
        )

    source_sentences = {
        source.source_id: set(split_source_sentences(source.excerpt)) for source in case.sources
    }

    evidence_counts = _new_counts()
    question_counts = _new_counts()

    valid_citations = 0
    invalid_citations = 0
    status_consistency_errors = 0

    for question in case.questions:
        finding = findings_by_question[question.question]

        expected_keys = {(item.source_id, item.excerpt) for item in question.expected_evidence}

        predicted_keys = {(item.source_id, item.excerpt) for item in finding.evidence}

        evidence_result = score_evidence(
            expected_keys,
            predicted_keys,
        )

        evidence_counts["true_positive"] += evidence_result["true_positive"]
        evidence_counts["false_positive"] += evidence_result["false_positive"]
        evidence_counts["false_negative"] += evidence_result["false_negative"]

        expected_supported = bool(expected_keys)
        predicted_supported = finding.status == "SUPPORTED"

        _record_binary_result(
            question_counts,
            expected_supported,
            predicted_supported,
        )

        if predicted_supported != bool(finding.evidence):
            status_consistency_errors += 1

        for evidence in finding.evidence:
            source_id = evidence.source_id

            if source_id in source_sentences and evidence.excerpt in source_sentences[source_id]:
                valid_citations += 1
            else:
                invalid_citations += 1

    return {
        "case_id": case.case_id,
        "title": case.title,
        "execution_status": response.status,
        "question_count": len(case.questions),
        "source_count": len(case.sources),
        "evidence": _evidence_metrics(evidence_counts),
        "question_answerability": _question_metrics(question_counts),
        "citation_traceability": _citation_metrics(
            valid_citations,
            invalid_citations,
        ),
        "status_consistency_errors": status_consistency_errors,
        "execution_latency_ms": round(execution_ms, 3),
    }


def evaluate_benchmark(
    benchmark: ResearchBenchmark,
) -> dict[str, Any]:
    """Run the complete benchmark and return an aggregate report."""

    overall_evidence_counts = _new_counts()
    overall_question_counts = _new_counts()

    total_valid_citations = 0
    total_invalid_citations = 0
    total_status_consistency_errors = 0

    case_results: list[dict[str, Any]] = []

    for case in benchmark.cases:
        result = _evaluate_case(case)
        case_results.append(result)

        evidence = result["evidence"]

        overall_evidence_counts["true_positive"] += evidence["true_positive"]
        overall_evidence_counts["false_positive"] += evidence["false_positive"]
        overall_evidence_counts["false_negative"] += evidence["false_negative"]

        questions = result["question_answerability"]

        for key in overall_question_counts:
            overall_question_counts[key] += questions[key]

        citations = result["citation_traceability"]

        total_valid_citations += citations["valid_evidence_count"]
        total_invalid_citations += citations["invalid_evidence_count"]

        total_status_consistency_errors += result["status_consistency_errors"]

    durations = [result["execution_latency_ms"] for result in case_results]

    total_questions = sum(len(case.questions) for case in benchmark.cases)

    return {
        "benchmark_id": benchmark.benchmark_id,
        "benchmark_version": benchmark.version,
        "graph_revision": benchmark.graph_revision,
        "benchmark_name": benchmark.name,
        "source_material": benchmark.source_material,
        "case_count": len(benchmark.cases),
        "question_count": total_questions,
        "overall": {
            "evidence": _evidence_metrics(overall_evidence_counts),
            "question_answerability": _question_metrics(overall_question_counts),
            "citation_traceability": _citation_metrics(
                total_valid_citations,
                total_invalid_citations,
            ),
            "status_consistency_errors": total_status_consistency_errors,
            "execution_latency": {
                "average_case_ms": round(statistics.mean(durations), 3),
                "median_case_ms": round(statistics.median(durations), 3),
                "maximum_case_ms": round(max(durations), 3),
            },
            "not_measured": [
                "Factual correctness of the authored source material.",
                "Authority, freshness or diversity of external sources.",
                "Research discovery recall beyond the supplied source corpus.",
                "Quality of natural-language synthesis or recommendations.",
                "Business usefulness of the final research report.",
            ],
        },
        "cases": case_results,
    }
