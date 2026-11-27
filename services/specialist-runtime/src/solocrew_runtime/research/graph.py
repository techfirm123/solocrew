import re
from datetime import UTC, datetime
from typing import TypedDict

from langgraph.graph import END, START, StateGraph

from solocrew_runtime.contracts.research import (
    ResearchEvidence,
    ResearchExecutionRequest,
    ResearchExecutionResponse,
    ResearchFinding,
)

_STOP_WORDS = frozenset(
    {
        "a",
        "an",
        "and",
        "are",
        "as",
        "at",
        "be",
        "by",
        "can",
        "could",
        "did",
        "does",
        "for",
        "from",
        "how",
        "in",
        "into",
        "is",
        "it",
        "its",
        "of",
        "on",
        "or",
        "should",
        "that",
        "the",
        "their",
        "them",
        "there",
        "this",
        "to",
        "was",
        "what",
        "when",
        "where",
        "which",
        "with",
        "would",
    }
)

_MINIMUM_RELEVANCE_SCORE = 0.30
_MINIMUM_MATCHED_TERMS = 2
_MAX_EVIDENCE_PER_QUESTION = 4


class ResearchGraphState(TypedDict):
    """State passed between research graph nodes."""

    request: ResearchExecutionRequest
    evidence_by_question: dict[str, list[ResearchEvidence]]
    response: ResearchExecutionResponse | None


def _normalize_token(token: str) -> str:
    """Apply basic deterministic singular normalization."""

    if len(token) > 4 and token.endswith("ies"):
        return token[:-3] + "y"

    if len(token) > 3 and token.endswith("s") and not token.endswith("ss"):
        return token[:-1]

    return token


def _meaningful_terms(value: str) -> set[str]:
    """Extract normalized words while excluding common stop words."""

    tokens = re.findall(r"[a-z0-9]+", value.casefold())

    return {
        _normalize_token(token)
        for token in tokens
        if token not in _STOP_WORDS and (len(token) > 2 or token in {"ai", "pc", "os"})
    }


def _split_sentences(value: str) -> list[str]:
    """Split source excerpts into candidate evidence sentences."""

    return [
        sentence.strip()
        for sentence in re.split(r"(?<=[.!?])\s+|\n+", value.strip())
        if sentence.strip()
    ]


def _prepare_research(
    state: ResearchGraphState,
) -> dict[str, dict[str, list[ResearchEvidence]]]:
    """Initialize an evidence collection for each research question."""

    return {
        "evidence_by_question": {question: [] for question in state["request"].research_questions}
    }


def _collect_evidence(
    state: ResearchGraphState,
) -> dict[str, dict[str, list[ResearchEvidence]]]:
    """Find and rank source sentences using question-focused term matching."""

    request = state["request"]
    evidence_by_question: dict[str, list[ResearchEvidence]] = {}

    for question in request.research_questions:
        # The question defines the evidence requirement.
        # The broader topic is context, not proof that a sentence answers it.
        question_terms = _meaningful_terms(question)

        ranked_matches: list[tuple[float, int, int, ResearchEvidence]] = []

        if not question_terms:
            evidence_by_question[question] = []
            continue

        for source_index, source in enumerate(request.sources):
            sentences = _split_sentences(source.excerpt)

            for sentence_index, sentence in enumerate(sentences):
                sentence_terms = _meaningful_terms(sentence)
                matched_terms = question_terms.intersection(sentence_terms)

                if len(matched_terms) < _MINIMUM_MATCHED_TERMS:
                    continue

                relevance_score = len(matched_terms) / len(question_terms)

                if relevance_score < _MINIMUM_RELEVANCE_SCORE:
                    continue

                evidence = ResearchEvidence(
                    source_id=source.source_id,
                    source_title=source.title,
                    source_url=source.source_url,
                    excerpt=sentence,
                    relevance_score=round(relevance_score, 3),
                )

                ranked_matches.append(
                    (
                        relevance_score,
                        source_index,
                        sentence_index,
                        evidence,
                    )
                )

        # Stable ordering ensures deterministic results for identical inputs.
        ranked_matches.sort(
            key=lambda item: (
                -item[0],
                item[1],
                item[2],
            )
        )

        evidence_by_question[question] = [
            item[3] for item in ranked_matches[:_MAX_EVIDENCE_PER_QUESTION]
        ]

    return {
        "evidence_by_question": evidence_by_question,
    }


def _assemble_report(
    state: ResearchGraphState,
) -> dict[str, ResearchExecutionResponse]:
    """Build the final report without inventing unsupported claims."""

    request = state["request"]
    evidence_by_question = state["evidence_by_question"]

    findings: list[ResearchFinding] = []

    for question in request.research_questions:
        evidence = evidence_by_question.get(question, [])

        findings.append(
            ResearchFinding(
                question=question,
                status=("SUPPORTED" if evidence else "NO_SUPPORTING_EVIDENCE"),
                evidence=evidence,
            )
        )

    supported_count = sum(finding.status == "SUPPORTED" for finding in findings)

    if supported_count == len(findings):
        execution_status = "COMPLETED"
    elif supported_count > 0:
        execution_status = "PARTIAL"
    else:
        execution_status = "INSUFFICIENT_EVIDENCE"

    summary = (
        f"Matching evidence was found for {supported_count} of {len(findings)} research questions."
    )

    limitations = [
        "Only source excerpts included in this request were analysed.",
        (
            "No external search, full-document retrieval, source verification, "
            "or AI-generated interpretation was performed."
        ),
        (
            "A missing finding means the supplied excerpts did not contain enough "
            "matching text; it does not prove that no answer exists."
        ),
    ]

    response = ResearchExecutionResponse(
        assignment_id=request.assignment_id,
        execution_id=request.execution_id,
        status=execution_status,
        title=f"Research brief: {request.topic}",
        summary=summary,
        findings=findings,
        limitations=limitations,
        source_count=len(request.sources),
        generated_at=datetime.now(UTC),
    )

    return {
        "response": response,
    }


def _build_graph():
    """Construct and compile the deterministic research graph."""

    builder = StateGraph(ResearchGraphState)

    builder.add_node("prepare_research", _prepare_research)
    builder.add_node("collect_evidence", _collect_evidence)
    builder.add_node("assemble_report", _assemble_report)

    builder.add_edge(START, "prepare_research")
    builder.add_edge("prepare_research", "collect_evidence")
    builder.add_edge("collect_evidence", "assemble_report")
    builder.add_edge("assemble_report", END)

    return builder.compile()


_RESEARCH_GRAPH = _build_graph()


def execute_research_request(
    request: ResearchExecutionRequest,
) -> ResearchExecutionResponse:
    """Execute the research graph for one validated request."""

    result = _RESEARCH_GRAPH.invoke(
        {
            "request": request,
            "evidence_by_question": {},
            "response": None,
        }
    )

    response = result["response"]

    if response is None:
        raise RuntimeError("Research graph completed without producing a response.")

    return response
