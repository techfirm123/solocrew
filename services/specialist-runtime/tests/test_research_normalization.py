from solocrew_runtime.research.normalization import (
    NORMALIZATION_REVISION,
    meaningful_terms,
    normalization_trace,
)


def test_controlled_aliases_match_paraphrased_thermal_vocabulary() -> None:
    question_terms = meaningful_terms("What makes a laptop run hot?")
    source_terms = meaningful_terms("High processor load raises internal operating temperature.")

    shared_terms = question_terms.intersection(source_terms)

    assert "causal_relation" in shared_terms
    assert "temperature_condition" in shared_terms

    assert NORMALIZATION_REVISION == "controlled-concept-aliases-v1"


def test_normalization_trace_explains_question_aliases() -> None:
    trace = normalization_trace("What makes a laptop run hot?")

    by_original_token = {item["original_token"]: item for item in trace}

    assert by_original_token["makes"]["stemmed_token"] == "make"
    assert by_original_token["makes"]["normalized_term"] == "causal_relation"
    assert by_original_token["makes"]["concept_group"] == "causal_relation"

    assert by_original_token["hot"]["normalized_term"] == "temperature_condition"
    assert by_original_token["hot"]["concept_group"] == "temperature_condition"


def test_temperature_plural_is_normalized_to_controlled_concept() -> None:
    terms = meaningful_terms("High operating temperatures affect laptop performance.")

    assert "temperature_condition" in terms


def test_unmapped_thermal_words_remain_distinct() -> None:
    terms = meaningful_terms("Thermal paste transfers heat during normal operation.")

    # These deliberately remain independent terms. Mapping them into one
    # broad thermal concept would create false-positive matches elsewhere.
    assert "thermal" in terms
    assert "heat" in terms
    assert "temperature_condition" not in terms


def test_unmapped_business_terms_remain_unchanged() -> None:
    assert meaningful_terms("battery runtime") == {
        "battery",
        "runtime",
    }
