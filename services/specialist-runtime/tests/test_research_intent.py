from uuid import uuid4

from solocrew_runtime.contracts.research import ResearchExecutionRequest
from solocrew_runtime.research.graph import execute_research_request
from solocrew_runtime.research.intent import (
    evaluate_candidate_intent,
    extract_question_intent,
)
from solocrew_runtime.research.normalization import meaningful_terms


def _build_request(
    question: str,
    excerpt: str,
) -> ResearchExecutionRequest:
    return ResearchExecutionRequest(
        assignment_id=uuid4(),
        execution_id=uuid4(),
        topic="Laptop performance and battery life",
        research_questions=[question],
        sources=[
            {
                "source_id": "source-001",
                "title": "Test source",
                "source_url": None,
                "excerpt": excerpt,
            }
        ],
    )


def test_extracts_subject_predicate_and_outcome() -> None:
    intent = extract_question_intent("Does replacing a CPU improve battery life?")

    assert intent.enabled is True
    assert intent.predicate == "improve"
    assert intent.subject_terms == {"replace", "cpu"}
    assert intent.outcome_terms == {"battery", "life"}


def test_does_not_apply_outcome_intent_to_open_questions() -> None:
    intent = extract_question_intent("What makes a laptop run hot?")

    assert intent.enabled is False
    assert intent.intent_type == "NONE"


def test_candidate_requires_at_least_one_subject_term() -> None:
    intent = extract_question_intent("Can screen damage affect SSD write endurance?")

    evaluation = evaluate_candidate_intent(
        intent,
        meaningful_terms(
            "SMART wear indicators provide an early warning of declining drive health."
        ),
    )

    assert evaluation.eligible is False
    assert evaluation.reason == "MISSING_REQUIRED_SUBJECT_TERMS"


def test_candidate_requires_all_outcome_terms() -> None:
    intent = extract_question_intent("Does RAM improve battery life?")

    evaluation = evaluate_candidate_intent(
        intent,
        meaningful_terms("Additional RAM can improve application multitasking."),
    )

    assert evaluation.eligible is False
    assert evaluation.reason == "MISSING_REQUIRED_OUTCOME_TERMS"
    assert evaluation.matched_subject_terms == {"ram"}
    assert evaluation.missing_outcome_terms == {"battery", "life"}


def test_candidate_is_eligible_when_subject_and_outcome_are_present() -> None:
    intent = extract_question_intent("Does RAM improve battery life?")

    evaluation = evaluate_candidate_intent(
        intent,
        meaningful_terms("A RAM upgrade can improve battery life by reducing disk activity."),
    )

    assert evaluation.eligible is True
    assert evaluation.reason == "INTENT_CONSTRAINTS_SATISFIED"
    assert evaluation.matched_subject_terms == {"ram"}
    assert evaluation.matched_outcome_terms == {"battery", "life"}


def test_graph_rejects_subject_overlap_without_requested_outcome() -> None:
    response = execute_research_request(
        _build_request(
            "Does RAM improve battery life?",
            "Additional RAM can improve application multitasking when several programs are open.",
        )
    )

    finding = response.findings[0]

    assert response.status == "INSUFFICIENT_EVIDENCE"
    assert finding.status == "NO_SUPPORTING_EVIDENCE"
    assert finding.evidence == []


def test_graph_accepts_matching_subject_and_requested_outcome() -> None:
    response = execute_research_request(
        _build_request(
            "Does RAM improve battery life?",
            "A RAM upgrade can improve battery life by reducing disk activity.",
        )
    )

    finding = response.findings[0]

    assert response.status == "COMPLETED"
    assert finding.status == "SUPPORTED"
    assert len(finding.evidence) == 1
    assert finding.evidence[0].source_id == "source-001"
