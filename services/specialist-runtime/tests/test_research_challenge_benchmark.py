from pathlib import Path

from solocrew_runtime.evaluation.benchmark import (
    evaluate_benchmark,
    load_benchmark,
)
from solocrew_runtime.evaluation.diagnostics import (
    build_benchmark_diagnostics,
)

CHALLENGE_BENCHMARK_PATH = (
    Path(__file__).resolve().parents[1] / "evaluation" / "benchmarks" / "research-challenge-v1.json"
)


def _load_challenge_benchmark():
    return load_benchmark(CHALLENGE_BENCHMARK_PATH)


def _question_by_case(report: dict, case_id: str) -> dict:
    """Return the single question diagnostic for a challenge case."""

    case = next(item for item in report["cases"] if item["case_id"] == case_id)

    assert len(case["question_diagnostics"]) == 1

    return case["question_diagnostics"][0]


def _candidate_by_source(question: dict, source_id: str) -> dict:
    """Return the candidate from a particular source."""

    return next(
        candidate
        for candidate in question["matching"]["candidates"]
        if candidate["source_id"] == source_id
    )


def test_challenge_benchmark_contains_five_distinct_scenarios() -> None:
    benchmark = _load_challenge_benchmark()

    assert benchmark.benchmark_id == "solocrew-personal-research-challenge"
    assert benchmark.version == "1.0.0"
    assert benchmark.graph_revision == "deterministic-question-focused-v4"
    assert len(benchmark.cases) == 5
    assert sum(len(case.questions) for case in benchmark.cases) == 5

    assert {case.case_id for case in benchmark.cases} == {
        "paraphrased-thermal-question",
        "short-keyword-overlap",
        "high-overlap-distractor",
        "multi-sentence-battery-evidence",
        "unsupported-screen-ssd-question",
    }


def test_challenge_benchmark_measures_v4_matching_improvements() -> None:
    benchmark = _load_challenge_benchmark()

    report = evaluate_benchmark(benchmark)

    evidence = report["overall"]["evidence"]
    answerability = report["overall"]["question_answerability"]

    assert report["case_count"] == 5
    assert report["question_count"] == 5

    assert evidence["true_positive"] == 3
    assert evidence["false_positive"] == 0
    assert evidence["false_negative"] == 0
    assert evidence["precision"] == 1.0
    assert evidence["recall"] == 1.0

    assert answerability["true_positive"] == 2
    assert answerability["false_positive"] == 0
    assert answerability["false_negative"] == 0
    assert answerability["true_negative"] == 3
    assert answerability["total_questions"] == 5
    assert answerability["answerability_accuracy"] == 1.0
    assert answerability["unsupported_question_rejection_rate"] == 1.0
    assert answerability["false_support_rate"] == 0.0

    assert report["overall"]["status_consistency_errors"] == 0


def test_paraphrased_question_remains_supported() -> None:
    benchmark = _load_challenge_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    question = _question_by_case(
        report,
        "paraphrased-thermal-question",
    )

    assert question["answerability"]["classification"] == "TRUE_POSITIVE"
    assert question["answerability"]["expected_supported"] is True
    assert question["answerability"]["predicted_supported"] is True

    assert question["evidence"]["expected_count"] == 1
    assert question["evidence"]["returned_count"] == 1
    assert question["evidence"]["false_negative"] == []


def test_short_keyword_overlap_is_rejected_by_outcome_matching() -> None:
    benchmark = _load_challenge_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    question = _question_by_case(
        report,
        "short-keyword-overlap",
    )

    assert question["answerability"]["classification"] == "TRUE_NEGATIVE"
    assert question["evidence"]["expected_count"] == 0
    assert question["evidence"]["returned_count"] == 0
    assert question["evidence"]["returned"] == []

    candidate = _candidate_by_source(
        question,
        "memory-upgrade-01",
    )

    assert candidate["selected"] is False
    assert candidate["selection_reason"] == "MISSING_REQUIRED_OUTCOME_TERMS"
    assert candidate["intent_evaluation"]["missing_outcome_terms"] == [
        "battery",
        "life",
    ]


def test_high_overlap_distractor_is_rejected_by_outcome_matching() -> None:
    benchmark = _load_challenge_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    question = _question_by_case(
        report,
        "high-overlap-distractor",
    )

    assert question["answerability"]["classification"] == "TRUE_NEGATIVE"
    assert question["evidence"]["expected_count"] == 0
    assert question["evidence"]["returned_count"] == 0
    assert question["evidence"]["returned"] == []

    candidate = _candidate_by_source(
        question,
        "cpu-repair-01",
    )

    assert candidate["selected"] is False
    assert candidate["selection_reason"] == "MISSING_REQUIRED_OUTCOME_TERMS"
    assert candidate["intent_evaluation"]["missing_outcome_terms"] == [
        "battery",
        "life",
    ]


def test_multi_sentence_question_returns_both_expected_evidence_items() -> None:
    benchmark = _load_challenge_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    question = _question_by_case(
        report,
        "multi-sentence-battery-evidence",
    )

    assert question["answerability"]["classification"] == "TRUE_POSITIVE"
    assert question["evidence"]["expected_count"] == 2
    assert question["evidence"]["returned_count"] == 2
    assert question["evidence"]["metrics"]["true_positive"] == 2
    assert question["evidence"]["false_positive"] == []
    assert question["evidence"]["false_negative"] == []

    returned_source_ids = {item["source_id"] for item in question["evidence"]["returned"]}

    assert returned_source_ids == {
        "battery-hazard-01",
        "battery-handling-01",
    }


def test_unsupported_control_question_is_rejected() -> None:
    benchmark = _load_challenge_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    question = _question_by_case(
        report,
        "unsupported-screen-ssd-question",
    )

    assert question["answerability"]["classification"] == "TRUE_NEGATIVE"
    assert question["answerability"]["predicted_supported"] is False
    assert question["evidence"]["returned"] == []

    candidate = _candidate_by_source(
        question,
        "ssd-health-02",
    )

    assert candidate["selection_reason"] == "MISSING_REQUIRED_SUBJECT_TERMS"


def test_challenge_diagnostics_match_actual_graph_selection() -> None:
    benchmark = _load_challenge_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    assert report["case_count"] == 5
    assert report["question_count"] == 5
    assert report["graph_revision"] == "deterministic-question-focused-v4"
    assert report["status_consistency_errors"] == 0
    assert report["selection_consistency_errors"] == 0

    for case in report["cases"]:
        assert case["selection_consistency_errors"] == 0

        for question in case["question_diagnostics"]:
            assert question["matching"]["selection_consistency_errors"] == 0
