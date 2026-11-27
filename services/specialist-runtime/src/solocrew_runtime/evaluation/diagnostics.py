from __future__ import annotations

from typing import Any
from uuid import uuid4

from solocrew_runtime.contracts.research import ResearchExecutionRequest
from solocrew_runtime.evaluation.benchmark import (
    BenchmarkCase,
    ResearchBenchmark,
    score_evidence,
)
from solocrew_runtime.research.graph import (
    _MAX_EVIDENCE_PER_QUESTION,
    _MINIMUM_MATCHED_TERMS,
    _MINIMUM_RELEVANCE_SCORE,
    GRAPH_REVISION,
    _meaningful_terms,
    _split_sentences,
    execute_research_request,
)
from solocrew_runtime.research.intent import (
    INTENT_REVISION,
    evaluate_candidate_intent,
    extract_question_intent,
)
from solocrew_runtime.research.normalization import (
    NORMALIZATION_REVISION,
    normalization_trace,
)

EvidenceKey = tuple[str, str]


def _build_request(case: BenchmarkCase) -> ResearchExecutionRequest:
    """Build the same validated execution request used by the benchmark."""

    return ResearchExecutionRequest(
        assignment_id=uuid4(),
        execution_id=uuid4(),
        topic=case.topic,
        research_questions=[question.question for question in case.questions],
        sources=case.sources,
    )


def _classification(
    expected_supported: bool,
    predicted_supported: bool,
) -> str:
    """Classify one answerability decision."""

    if expected_supported and predicted_supported:
        return "TRUE_POSITIVE"

    if not expected_supported and predicted_supported:
        return "FALSE_POSITIVE"

    if expected_supported and not predicted_supported:
        return "FALSE_NEGATIVE"

    return "TRUE_NEGATIVE"


def _evidence_key_items(
    keys: set[EvidenceKey],
    source_titles: dict[str, str],
) -> list[dict[str, str]]:
    """Create stable, readable evidence comparison entries."""

    return [
        {
            "source_id": source_id,
            "source_title": source_titles.get(source_id, "Unknown source"),
            "excerpt": excerpt,
        }
        for source_id, excerpt in sorted(keys)
    ]


def _returned_evidence_items(finding: Any) -> list[dict[str, Any]]:
    """Serialize the evidence actually returned by the research graph."""

    return [
        {
            "source_id": evidence.source_id,
            "source_title": evidence.source_title,
            "source_url": str(evidence.source_url) if evidence.source_url else None,
            "excerpt": evidence.excerpt,
            "relevance_score": evidence.relevance_score,
        }
        for evidence in finding.evidence
    ]


def _matching_diagnostics(
    request: ResearchExecutionRequest,
    question: str,
    returned_evidence: list[Any],
) -> dict[str, Any]:
    """Explain how deterministic matching evaluates every source sentence."""

    question_terms = _meaningful_terms(question)
    topic_context_terms = _meaningful_terms(request.topic)
    question_intent = extract_question_intent(question)

    candidates: list[dict[str, Any]] = []
    eligible_candidates: list[tuple[float, int, int, int]] = []

    for source_index, source in enumerate(request.sources):
        sentences = _split_sentences(source.excerpt)

        for sentence_index, sentence in enumerate(sentences):
            sentence_terms = _meaningful_terms(sentence)
            matched_terms = question_terms.intersection(sentence_terms)
            topic_terms_matched = topic_context_terms.intersection(sentence_terms)

            relevance_score = len(matched_terms) / len(question_terms) if question_terms else 0.0

            intent_evaluation = evaluate_candidate_intent(
                question_intent,
                sentence_terms,
            )

            # Preserve the established lexical-threshold diagnostic when
            # a candidate has some question overlap but fewer than two terms.
            # With no overlap, intent diagnostics provide the more useful
            # explanation when a recognized intent constraint is violated.
            if matched_terms and len(matched_terms) < _MINIMUM_MATCHED_TERMS:
                selection_reason = "BELOW_MINIMUM_MATCHED_TERMS"
            elif not intent_evaluation.eligible:
                selection_reason = intent_evaluation.reason
            elif not matched_terms:
                selection_reason = "NO_QUESTION_TERM_OVERLAP"
            elif len(matched_terms) < _MINIMUM_MATCHED_TERMS:
                selection_reason = "BELOW_MINIMUM_MATCHED_TERMS"
            elif relevance_score < _MINIMUM_RELEVANCE_SCORE:
                selection_reason = "BELOW_MINIMUM_RELEVANCE_SCORE"
            else:
                selection_reason = "PENDING_RANKING"

            candidate = {
                "candidate_id": f"{source_index}:{sentence_index}",
                "source_id": source.source_id,
                "source_title": source.title,
                "sentence_index": sentence_index,
                "excerpt": sentence,
                "normalized_terms": sorted(sentence_terms),
                "normalization_trace": normalization_trace(sentence),
                "matched_terms": sorted(matched_terms),
                "topic_context_terms_matched": sorted(topic_terms_matched),
                "intent_evaluation": intent_evaluation.to_dict(),
                "relevance_score": round(relevance_score, 3),
                "matched_term_count": len(matched_terms),
                "eligible": selection_reason == "PENDING_RANKING",
                "selected": False,
                "selection_rank": None,
                "selection_reason": selection_reason,
            }

            candidate_index = len(candidates)
            candidates.append(candidate)

            if candidate["eligible"]:
                eligible_candidates.append(
                    (
                        relevance_score,
                        source_index,
                        sentence_index,
                        candidate_index,
                    )
                )

    # This is the same stable ordering used by the research graph.
    eligible_candidates.sort(
        key=lambda item: (
            -item[0],
            item[1],
            item[2],
        )
    )

    matcher_selected_keys: list[EvidenceKey] = []

    for rank, (_, _, _, candidate_index) in enumerate(
        eligible_candidates,
        start=1,
    ):
        candidate = candidates[candidate_index]
        candidate["selection_rank"] = rank

        if rank <= _MAX_EVIDENCE_PER_QUESTION:
            candidate["selected"] = True
            candidate["selection_reason"] = "SELECTED"
            matcher_selected_keys.append(
                (
                    candidate["source_id"],
                    candidate["excerpt"],
                )
            )
        else:
            candidate["selection_reason"] = "EXCLUDED_BY_EVIDENCE_LIMIT"

    actual_returned_keys = [
        (evidence.source_id, evidence.excerpt) for evidence in returned_evidence
    ]

    selection_consistency_errors = int(matcher_selected_keys != actual_returned_keys)

    return {
        "question_terms": sorted(question_terms),
        "question_normalization_trace": normalization_trace(question),
        "topic_context_terms": sorted(topic_context_terms),
        "topic_terms_used_for_scoring": False,
        "question_intent": question_intent.to_dict(),
        "intent_revision": INTENT_REVISION,
        "minimum_matched_terms": _MINIMUM_MATCHED_TERMS,
        "minimum_relevance_score": _MINIMUM_RELEVANCE_SCORE,
        "maximum_evidence_per_question": _MAX_EVIDENCE_PER_QUESTION,
        "candidate_sentence_count": len(candidates),
        "eligible_candidate_count": len(eligible_candidates),
        "selected_candidate_count": len(matcher_selected_keys),
        "selection_consistency_errors": selection_consistency_errors,
        "matcher_selected_evidence": [
            {
                "source_id": source_id,
                "excerpt": excerpt,
            }
            for source_id, excerpt in matcher_selected_keys
        ],
        "actual_returned_evidence": [
            {
                "source_id": source_id,
                "excerpt": excerpt,
            }
            for source_id, excerpt in actual_returned_keys
        ],
        "candidates": candidates,
    }


def _diagnose_question(
    request: ResearchExecutionRequest,
    case: BenchmarkCase,
    question: Any,
    finding: Any,
) -> dict[str, Any]:
    """Build a detailed diagnostic record for one research question."""

    expected_keys = {(item.source_id, item.excerpt) for item in question.expected_evidence}

    predicted_keys = {(item.source_id, item.excerpt) for item in finding.evidence}

    true_positive_keys = expected_keys.intersection(predicted_keys)
    false_positive_keys = predicted_keys.difference(expected_keys)
    false_negative_keys = expected_keys.difference(predicted_keys)

    source_titles = {source.source_id: source.title for source in case.sources}

    expected_supported = bool(expected_keys)
    predicted_supported = finding.status == "SUPPORTED"

    return {
        "question": question.question,
        "answerability": {
            "expected_supported": expected_supported,
            "predicted_supported": predicted_supported,
            "classification": _classification(
                expected_supported,
                predicted_supported,
            ),
            "returned_finding_status": finding.status,
            "status_consistent_with_evidence": (predicted_supported == bool(finding.evidence)),
        },
        "evidence": {
            "expected_count": len(expected_keys),
            "returned_count": len(predicted_keys),
            "metrics": score_evidence(
                expected_keys,
                predicted_keys,
            ),
            "expected": _evidence_key_items(
                expected_keys,
                source_titles,
            ),
            "returned": _returned_evidence_items(finding),
            "true_positive": _evidence_key_items(
                true_positive_keys,
                source_titles,
            ),
            "false_positive": _evidence_key_items(
                false_positive_keys,
                source_titles,
            ),
            "false_negative": _evidence_key_items(
                false_negative_keys,
                source_titles,
            ),
        },
        "matching": _matching_diagnostics(
            request,
            question.question,
            finding.evidence,
        ),
    }


def build_benchmark_diagnostics(
    benchmark: ResearchBenchmark,
) -> dict[str, Any]:
    """Execute the benchmark again and return question-level diagnostics."""

    case_results: list[dict[str, Any]] = []
    total_questions = 0
    total_status_consistency_errors = 0
    total_selection_consistency_errors = 0

    for case in benchmark.cases:
        request = _build_request(case)
        response = execute_research_request(request)

        findings_by_question = {finding.question: finding for finding in response.findings}

        expected_questions = {question.question for question in case.questions}

        if set(findings_by_question) != expected_questions:
            raise ValueError(
                "Research graph returned an unexpected question set "
                f"for benchmark case '{case.case_id}'."
            )

        question_diagnostics = [
            _diagnose_question(
                request,
                case,
                question,
                findings_by_question[question.question],
            )
            for question in case.questions
        ]

        case_status_errors = sum(
            not item["answerability"]["status_consistent_with_evidence"]
            for item in question_diagnostics
        )

        case_selection_errors = sum(
            item["matching"]["selection_consistency_errors"] for item in question_diagnostics
        )

        total_questions += len(question_diagnostics)
        total_status_consistency_errors += case_status_errors
        total_selection_consistency_errors += case_selection_errors

        case_results.append(
            {
                "case_id": case.case_id,
                "title": case.title,
                "execution_status": response.status,
                "source_count": len(case.sources),
                "question_count": len(question_diagnostics),
                "status_consistency_errors": case_status_errors,
                "selection_consistency_errors": case_selection_errors,
                "question_diagnostics": question_diagnostics,
            }
        )

    return {
        "diagnostic_schema_version": "1.0.0",
        "benchmark_id": benchmark.benchmark_id,
        "benchmark_version": benchmark.version,
        "graph_revision": GRAPH_REVISION,
        "normalization_revision": NORMALIZATION_REVISION,
        "intent_revision": INTENT_REVISION,
        "case_count": len(case_results),
        "question_count": total_questions,
        "status_consistency_errors": total_status_consistency_errors,
        "selection_consistency_errors": total_selection_consistency_errors,
        "explanation": (
            "This report explains deterministic question-focused matching. "
            "Topic terms are retained as context but are not evidence of relevance. "
            "Recognized yes/no outcome questions receive additional subject and "
            "outcome constraints. A selected sentence is not proof of factual "
            "correctness or semantic support. Source quality and external research "
            "discovery are outside this diagnostic."
        ),
        "cases": case_results,
    }
