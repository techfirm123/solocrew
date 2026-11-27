from __future__ import annotations

import re

NORMALIZATION_REVISION = "controlled-concept-aliases-v1"

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

# These groups are deliberately small and explicit.
#
# They represent controlled concept aliases, not unrestricted semantic
# equivalence. Review every addition against both benchmark suites.
_CONCEPT_GROUPS: dict[str, frozenset[str]] = {
    "temperature_condition": frozenset(
        {
            "hot",
            "hotter",
            "overheat",
            "overheated",
            "overheating",
            "temperature",
        }
    ),
    "causal_relation": frozenset(
        {
            "cause",
            "causing",
            "increase",
            "increasing",
            "make",
            "made",
            "raise",
            "raised",
        }
    ),
}

_ALIAS_TO_CONCEPT = {
    alias: concept for concept, aliases in _CONCEPT_GROUPS.items() for alias in aliases
}

_TOKEN_PATTERN = re.compile(r"[a-z0-9]+")


def _normalize_morphology(token: str) -> str:
    """Apply the existing basic deterministic singular normalization."""

    if len(token) > 4 and token.endswith("ies"):
        return token[:-3] + "y"

    if len(token) > 3 and token.endswith("s") and not token.endswith("ss"):
        return token[:-1]

    return token


def normalize_token(token: str) -> tuple[str, str]:
    """
    Return the morphologically normalized token and its canonical term.

    The second value is either a controlled concept group or the
    morphologically normalized token itself.
    """

    stemmed_token = _normalize_morphology(token.casefold())
    canonical_term = _ALIAS_TO_CONCEPT.get(stemmed_token, stemmed_token)

    return stemmed_token, canonical_term


def meaningful_terms(value: str) -> set[str]:
    """Extract normalized terms while excluding common stop words."""

    result: set[str] = set()

    for token in _TOKEN_PATTERN.findall(value.casefold()):
        if token in _STOP_WORDS:
            continue

        if len(token) <= 2 and token not in {"ai", "pc", "os"}:
            continue

        _, canonical_term = normalize_token(token)
        result.add(canonical_term)

    return result


def normalization_trace(value: str) -> list[dict[str, str | None]]:
    """
    Explain how meaningful input tokens are normalized.

    Stop words and tokens excluded by the minimum token length are omitted.
    """

    trace: list[dict[str, str | None]] = []

    for token in _TOKEN_PATTERN.findall(value.casefold()):
        if token in _STOP_WORDS:
            continue

        if len(token) <= 2 and token not in {"ai", "pc", "os"}:
            continue

        stemmed_token, canonical_term = normalize_token(token)

        concept_group = canonical_term if canonical_term in _CONCEPT_GROUPS else None

        trace.append(
            {
                "original_token": token,
                "stemmed_token": stemmed_token,
                "normalized_term": canonical_term,
                "concept_group": concept_group,
            }
        )

    return trace
