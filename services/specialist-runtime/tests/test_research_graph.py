from uuid import uuid4

from solocrew_runtime.contracts.research import ResearchExecutionRequest
from solocrew_runtime.research.graph import execute_research_request


def build_request() -> ResearchExecutionRequest:
    return ResearchExecutionRequest(
        assignment_id=uuid4(),
        execution_id=uuid4(),
        topic="Laptop battery health",
        research_questions=[
            "How does battery age affect battery capacity?",
            "What causes a laptop charger to overheat?",
        ],
        sources=[
            {
                "source_id": "source-001",
                "title": "Battery maintenance notes",
                "source_url": None,
                "excerpt": (
                    "As lithium-ion batteries age, their maximum charge capacity "
                    "gradually decreases. High operating temperatures can accelerate "
                    "battery degradation."
                ),
            }
        ],
    )


def test_graph_returns_evidence_for_supported_question() -> None:
    request = build_request()

    response = execute_research_request(request)

    first_finding = response.findings[0]

    assert response.status == "PARTIAL"
    assert first_finding.status == "SUPPORTED"
    assert len(first_finding.evidence) > 0
    assert first_finding.evidence[0].source_id == "source-001"


def test_graph_does_not_invent_unsupported_answers() -> None:
    response = execute_research_request(build_request())

    second_finding = response.findings[1]

    assert second_finding.status == "NO_SUPPORTING_EVIDENCE"
    assert second_finding.evidence == []

    assert any("does not prove" in limitation for limitation in response.limitations)


def test_graph_returns_insufficient_evidence_when_nothing_matches() -> None:
    request = ResearchExecutionRequest(
        assignment_id=uuid4(),
        execution_id=uuid4(),
        topic="Laptop battery health",
        research_questions=[
            "How can a laptop keyboard be replaced?",
        ],
        sources=[
            {
                "source_id": "source-002",
                "title": "Battery notes",
                "source_url": None,
                "excerpt": "Battery capacity gradually decreases as lithium-ion cells age.",
            }
        ],
    )

    response = execute_research_request(request)

    assert response.status == "INSUFFICIENT_EVIDENCE"
    assert response.findings[0].status == "NO_SUPPORTING_EVIDENCE"
    assert response.findings[0].evidence == []


def test_graph_rejects_topic_overlap_as_the_only_evidence() -> None:
    request = ResearchExecutionRequest(
        assignment_id=uuid4(),
        execution_id=uuid4(),
        topic="Laptop battery health",
        research_questions=[
            "Does replacing RAM improve battery runtime?",
        ],
        sources=[
            {
                "source_id": "source-003",
                "title": "Battery storage notes",
                "source_url": None,
                "excerpt": (
                    "A laptop battery should be stored with a partial charge "
                    "during long periods of inactivity."
                ),
            }
        ],
    )

    response = execute_research_request(request)

    finding = response.findings[0]

    assert response.status == "INSUFFICIENT_EVIDENCE"
    assert finding.status == "NO_SUPPORTING_EVIDENCE"
    assert finding.evidence == []


def test_graph_preserves_assignment_and_execution_ids() -> None:
    request = build_request()

    response = execute_research_request(request)

    assert response.assignment_id == request.assignment_id
    assert response.execution_id == request.execution_id


def test_graph_preserves_source_traceability() -> None:
    response = execute_research_request(build_request())

    for finding in response.findings:
        for evidence in finding.evidence:
            assert evidence.source_id == "source-001"
            assert evidence.source_title == "Battery maintenance notes"
