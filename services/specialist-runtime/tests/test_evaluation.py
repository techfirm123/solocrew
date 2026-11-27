import json
from pathlib import Path

import pytest
from pydantic import ValidationError

from solocrew_runtime.evaluation.benchmark import (
    ResearchBenchmark,
    evaluate_benchmark,
    load_benchmark,
    score_evidence,
)

BENCHMARK_PATH = (
    Path(__file__).resolve().parents[1] / "evaluation" / "benchmarks" / "research-v1.json"
)


def test_initial_benchmark_contains_three_cases_and_thirteen_questions() -> None:
    benchmark = load_benchmark(BENCHMARK_PATH)

    assert benchmark.benchmark_id == "solocrew-personal-research"
    assert benchmark.version == "1.0.0"
    assert benchmark.graph_revision == "deterministic-question-focused-v2"
    assert len(benchmark.cases) == 3

    question_count = sum(len(case.questions) for case in benchmark.cases)

    assert question_count == 13


def test_benchmark_rejects_expected_evidence_not_in_source() -> None:
    payload = json.loads(BENCHMARK_PATH.read_text(encoding="utf-8"))

    payload["cases"][0]["questions"][0]["expected_evidence"][0]["excerpt"] = (
        "This sentence does not exist in the source."
    )

    with pytest.raises(ValidationError):
        ResearchBenchmark.model_validate(payload)


def test_evidence_scoring_uses_source_and_exact_sentence() -> None:
    expected = {
        ("source-001", "Expected evidence sentence."),
        ("source-002", "Another expected evidence sentence."),
    }

    predicted = {
        ("source-001", "Expected evidence sentence."),
        ("source-003", "Unrelated sentence."),
    }

    result = score_evidence(expected, predicted)

    assert result["true_positive"] == 1
    assert result["false_positive"] == 1
    assert result["false_negative"] == 1
    assert result["precision"] == 0.5
    assert result["recall"] == 0.5
    assert result["f1"] == 0.5


def test_benchmark_evaluation_returns_aggregate_metrics() -> None:
    benchmark = load_benchmark(BENCHMARK_PATH)

    result = evaluate_benchmark(benchmark)

    assert result["case_count"] == 3
    assert result["question_count"] == 13

    assert "precision" in result["overall"]["evidence"]
    assert "recall" in result["overall"]["evidence"]
    assert "f1" in result["overall"]["evidence"]

    assert result["overall"]["question_answerability"]["total_questions"] == 13

    assert result["overall"]["execution_latency"]["average_case_ms"] >= 0


def test_benchmark_evidence_is_traceable_to_input_sources() -> None:
    benchmark = load_benchmark(BENCHMARK_PATH)

    result = evaluate_benchmark(benchmark)

    citations = result["overall"]["citation_traceability"]

    assert citations["invalid_evidence_count"] == 0
    assert citations["valid_evidence_count"] > 0
    assert citations["traceability_rate"] == 1.0

    assert result["overall"]["status_consistency_errors"] == 0
