from __future__ import annotations

import re
from dataclasses import dataclass

from solocrew_runtime.research.normalization import (
    meaningful_terms,
    normalize_token,
)

INTENT_REVISION = "deterministic-outcome-intent-v1"

_YES_NO_STARTERS = frozenset(
    {
        "can",
        "could",
        "did",
        "do",
        "does",
        "is",
        "are",
        "should",
        "will",
        "would",
    }
)

_IMPACT_PREDICATES = frozenset(
    {
        "affect",
        "cause",
        "causing",
        "decrease",
        "decreases",
        "extend",
        "extends",
        "improve",
        "improves",
        "impact",
        "impacts",
        "increase",
        "increases",
        "lower",
        "lowers",
        "make",
        "made",
        "raise",
        "raises",
        "reduce",
        "reduces",
    }
)

# Narrow, explicit morphology normalization for intent matching.
# Keep this separate from general evidence normalization until
# broader benchmark coverage justifies expanding shared rules.
_INTENT_MORPHOLOGY_ALIASES = {
    "replace": "replace",
    "replacing": "replace",
    "replaced": "replace",
    "replaces": "replace",
}


@dataclass(frozen=True, slots=True)
class QuestionIntent:
    """Deterministic subject-predicate-outcome interpretation."""

    intent_type: str = "NONE"
    predicate: str | None = None
    subject_terms: frozenset[str] = frozenset()
    outcome_terms: frozenset[str] = frozenset()

    @property
    def enabled(self) -> bool:
        return self.intent_type == "OUTCOME_RELATIONSHIP"

    def to_dict(self) -> dict[str, object]:
        """Return a stable JSON-compatible representation."""

        return {
            "intent_type": self.intent_type,
            "predicate": self.predicate,
            "required_subject_terms": sorted(self.subject_terms),
            "required_outcome_terms": sorted(self.outcome_terms),
        }


@dataclass(frozen=True, slots=True)
class IntentEvaluation:
    """Result of applying question intent constraints to one sentence."""

    eligible: bool
    reason: str
    matched_subject_terms: frozenset[str] = frozenset()
    missing_subject_terms: frozenset[str] = frozenset()
    matched_outcome_terms: frozenset[str] = frozenset()
    missing_outcome_terms: frozenset[str] = frozenset()

    def to_dict(self) -> dict[str, object]:
        """Return a stable JSON-compatible representation."""

        return {
            "eligible": self.eligible,
            "reason": self.reason,
            "matched_subject_terms": sorted(self.matched_subject_terms),
            "missing_subject_terms": sorted(self.missing_subject_terms),
            "matched_outcome_terms": sorted(self.matched_outcome_terms),
            "missing_outcome_terms": sorted(self.missing_outcome_terms),
        }


def _normalize_intent_terms(terms: set[str]) -> frozenset[str]:
    """Apply narrow morphology normalization consistently to intent terms."""

    return frozenset(_INTENT_MORPHOLOGY_ALIASES.get(term, term) for term in terms)


def _normalized_predicate(token: str) -> str:
    """Apply the shared morphology rules to a predicate token."""

    normalized, _ = normalize_token(token)
    return normalized


def extract_question_intent(question: str) -> QuestionIntent:
    """
    Extract a bounded subject-predicate-outcome structure.

    Only questions beginning with a recognized yes/no starter are considered.
    The first recognized impact predicate separates the subject from the
    requested outcome.

    This is a heuristic, not a general-purpose language parser.
    """

    tokens = re.findall(r"[a-z0-9]+", question.casefold())

    if not tokens or tokens[0] not in _YES_NO_STARTERS:
        return QuestionIntent()

    for predicate_index in range(1, len(tokens)):
        predicate = _normalized_predicate(tokens[predicate_index])

        if predicate not in _IMPACT_PREDICATES:
            continue

        subject_terms = _normalize_intent_terms(
            meaningful_terms(" ".join(tokens[1:predicate_index]))
        )
        outcome_terms = _normalize_intent_terms(
            meaningful_terms(" ".join(tokens[predicate_index + 1 :]))
        )

        if not outcome_terms:
            return QuestionIntent()

        return QuestionIntent(
            intent_type="OUTCOME_RELATIONSHIP",
            predicate=predicate,
            subject_terms=subject_terms,
            outcome_terms=outcome_terms,
        )

    return QuestionIntent()


def evaluate_candidate_intent(
    intent: QuestionIntent,
    sentence_terms: set[str],
) -> IntentEvaluation:
    """
    Check whether a candidate sentence covers the requested relationship.

    At least one subject term must be present when subject terms exist.
    Every extracted outcome term must be present.

    The matcher deliberately does not infer synonyms here.
    """

    if not intent.enabled:
        return IntentEvaluation(
            eligible=True,
            reason="NO_INTENT_CONSTRAINT",
        )

    normalized_sentence_terms = _normalize_intent_terms(sentence_terms)

    matched_subject_terms = intent.subject_terms.intersection(normalized_sentence_terms)
    missing_subject_terms = intent.subject_terms.difference(normalized_sentence_terms)

    matched_outcome_terms = intent.outcome_terms.intersection(normalized_sentence_terms)
    missing_outcome_terms = intent.outcome_terms.difference(normalized_sentence_terms)

    if intent.subject_terms and not matched_subject_terms:
        return IntentEvaluation(
            eligible=False,
            reason="MISSING_REQUIRED_SUBJECT_TERMS",
            matched_subject_terms=frozenset(matched_subject_terms),
            missing_subject_terms=frozenset(missing_subject_terms),
            matched_outcome_terms=frozenset(matched_outcome_terms),
            missing_outcome_terms=frozenset(missing_outcome_terms),
        )

    if missing_outcome_terms:
        return IntentEvaluation(
            eligible=False,
            reason="MISSING_REQUIRED_OUTCOME_TERMS",
            matched_subject_terms=frozenset(matched_subject_terms),
            missing_subject_terms=frozenset(missing_subject_terms),
            matched_outcome_terms=frozenset(matched_outcome_terms),
            missing_outcome_terms=frozenset(missing_outcome_terms),
        )

    return IntentEvaluation(
        eligible=True,
        reason="INTENT_CONSTRAINTS_SATISFIED",
        matched_subject_terms=frozenset(matched_subject_terms),
        missing_subject_terms=frozenset(missing_subject_terms),
        matched_outcome_terms=frozenset(matched_outcome_terms),
        missing_outcome_terms=frozenset(missing_outcome_terms),
    )
