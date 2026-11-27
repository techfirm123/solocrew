from pathlib import Path

from solocrew_runtime.evaluation.benchmark import (
    evaluate_benchmark,
    load_benchmark,
)
from solocrew_runtime.evaluation.diagnostics import (
    build_benchmark_diagnostics,
)

BENCHMARK_PATH = (
    Path(__file__).resolve().parents[1] / "evaluation" / "benchmarks" / "research-v1.json"
)


def _load_current_benchmark():
    return load_benchmark(BENCHMARK_PATH)


def test_diagnostics_cover_all_benchmark_questions() -> None:
    benchmark = _load_current_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    assert report["diagnostic_schema_version"] == "1.0.0"
    assert report["benchmark_id"] == "solocrew-personal-research"
    assert report["case_count"] == 3
    assert report["question_count"] == 13

    assert sum(case["question_count"] for case in report["cases"]) == 13
    assert report["status_consistency_errors"] == 0
    assert report["selection_consistency_errors"] == 0


def test_diagnostic_counts_reconcile_with_original_benchmark() -> None:
    benchmark = _load_current_benchmark()

    baseline = evaluate_benchmark(benchmark)
    diagnostics = build_benchmark_diagnostics(benchmark)

    evidence_counts = {
        "true_positive": 0,
        "false_positive": 0,
        "false_negative": 0,
    }

    answerability_counts = {
        "TRUE_POSITIVE": 0,
        "FALSE_POSITIVE": 0,
        "FALSE_NEGATIVE": 0,
        "TRUE_NEGATIVE": 0,
    }

    for case in diagnostics["cases"]:
        for question in case["question_diagnostics"]:
            metrics = question["evidence"]["metrics"]

            for key in evidence_counts:
                evidence_counts[key] += metrics[key]

            classification = question["answerability"]["classification"]
            answerability_counts[classification] += 1

    baseline_evidence = baseline["overall"]["evidence"]
    baseline_answerability = baseline["overall"]["question_answerability"]

    assert evidence_counts == {
        "true_positive": baseline_evidence["true_positive"],
        "false_positive": baseline_evidence["false_positive"],
        "false_negative": baseline_evidence["false_negative"],
    }

    assert answerability_counts == {
        "TRUE_POSITIVE": baseline_answerability["true_positive"],
        "FALSE_POSITIVE": baseline_answerability["false_positive"],
        "FALSE_NEGATIVE": baseline_answerability["false_negative"],
        "TRUE_NEGATIVE": baseline_answerability["true_negative"],
    }


def test_question_focused_matcher_has_no_false_positives_on_current_benchmark() -> None:
    benchmark = _load_current_benchmark()

    baseline = evaluate_benchmark(benchmark)
    evidence = baseline["overall"]["evidence"]
    answerability = baseline["overall"]["question_answerability"]

    assert evidence["true_positive"] == 10
    assert evidence["false_positive"] == 0
    assert evidence["false_negative"] == 0
    assert evidence["precision"] == 1.0
    assert evidence["recall"] == 1.0
    assert evidence["f1"] == 1.0

    assert answerability["true_positive"] == 10
    assert answerability["false_positive"] == 0
    assert answerability["false_negative"] == 0
    assert answerability["true_negative"] == 3
    assert answerability["answerability_accuracy"] == 1.0
    assert answerability["unsupported_question_rejection_rate"] == 1.0
    assert answerability["false_support_rate"] == 0.0


def test_unsupported_ram_question_is_rejected() -> None:
    benchmark = _load_current_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    battery_case = next(
        case for case in report["cases"] if case["case_id"] == "laptop-battery-health"
    )

    question = next(
        item
        for item in battery_case["question_diagnostics"]
        if item["question"] == "Does replacing RAM improve battery runtime?"
    )

    assert question["answerability"]["classification"] == "TRUE_NEGATIVE"
    assert question["answerability"]["predicted_supported"] is False
    assert question["evidence"]["returned"] == []

    matching = question["matching"]

    assert matching["topic_terms_used_for_scoring"] is False

    storage_candidate = next(
        candidate
        for candidate in matching["candidates"]
        if candidate["excerpt"].startswith("A laptop battery should be stored")
    )

    assert storage_candidate["selected"] is False
    assert storage_candidate["matched_terms"] == ["battery"]
    assert storage_candidate["selection_reason"] == "BELOW_MINIMUM_MATCHED_TERMS"


def test_topic_terms_are_context_not_evidence() -> None:
    benchmark = _load_current_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    for case in report["cases"]:
        for question in case["question_diagnostics"]:
            matching = question["matching"]

            assert matching["topic_terms_used_for_scoring"] is False
            assert matching["minimum_matched_terms"] == 2
            assert matching["minimum_relevance_score"] == 0.30

            for candidate in matching["candidates"]:
                if candidate["selected"]:
                    assert len(candidate["matched_terms"]) >= 2
                    assert candidate["relevance_score"] >= 0.30


def test_matcher_diagnostics_explain_actual_evidence_selection() -> None:
    benchmark = _load_current_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    for case in report["cases"]:
        for question in case["question_diagnostics"]:
            matching = question["matching"]

            assert matching["selection_consistency_errors"] == 0

            selected_candidates = sorted(
                (candidate for candidate in matching["candidates"] if candidate["selected"]),
                key=lambda candidate: candidate["selection_rank"],
            )

            selected_keys = [
                (candidate["source_id"], candidate["excerpt"]) for candidate in selected_candidates
            ]

            returned_keys = [
                (item["source_id"], item["excerpt"]) for item in question["evidence"]["returned"]
            ]

            assert selected_keys == returned_keys


def test_no_question_has_false_positive_or_false_negative_evidence() -> None:
    benchmark = _load_current_benchmark()

    report = build_benchmark_diagnostics(benchmark)

    for case in report["cases"]:
        for question in case["question_diagnostics"]:
            assert question["evidence"]["false_positive"] == []
            assert question["evidence"]["false_negative"] == []
            assert question["answerability"]["classification"] in {
                "TRUE_POSITIVE",
                "TRUE_NEGATIVE",
            }
