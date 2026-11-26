from datetime import datetime
from typing import Literal
from uuid import UUID

from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    HttpUrl,
    field_validator,
    model_validator,
)


class ResearchSourceInput(BaseModel):
    """A source excerpt supplied by a trusted caller."""

    model_config = ConfigDict(
        extra="forbid",
        str_strip_whitespace=True,
    )

    source_id: str = Field(min_length=1, max_length=80)
    title: str = Field(min_length=1, max_length=200)
    source_url: HttpUrl | None = None
    excerpt: str = Field(min_length=20, max_length=20_000)


class ResearchExecutionRequest(BaseModel):
    """Input contract for one bounded research execution."""

    model_config = ConfigDict(
        extra="forbid",
        str_strip_whitespace=True,
    )

    assignment_id: UUID
    execution_id: UUID

    topic: str = Field(min_length=5, max_length=250)
    research_questions: list[str] = Field(min_length=1, max_length=8)
    sources: list[ResearchSourceInput] = Field(min_length=1, max_length=20)

    @field_validator("topic")
    @classmethod
    def normalize_topic(cls, value: str) -> str:
        return " ".join(value.split())

    @field_validator("research_questions")
    @classmethod
    def normalize_questions(cls, value: list[str]) -> list[str]:
        normalized = [" ".join(question.split()) for question in value]

        if any(not question for question in normalized):
            raise ValueError("Research questions must not be empty.")

        return normalized

    @model_validator(mode="after")
    def validate_research_material(self) -> "ResearchExecutionRequest":
        question_keys = [question.casefold() for question in self.research_questions]

        if len(question_keys) != len(set(question_keys)):
            raise ValueError("Research questions must be unique.")

        source_ids = [source.source_id for source in self.sources]

        if len(source_ids) != len(set(source_ids)):
            raise ValueError("Source IDs must be unique within an execution.")

        total_excerpt_length = sum(len(source.excerpt) for source in self.sources)

        if total_excerpt_length > 80_000:
            raise ValueError("Combined source excerpt length must not exceed 80,000 characters.")

        return self


class ResearchEvidence(BaseModel):
    """An exact source excerpt that supports a research finding."""

    source_id: str
    source_title: str
    source_url: HttpUrl | None = None
    excerpt: str
    relevance_score: float = Field(ge=0.0, le=1.0)


class ResearchFinding(BaseModel):
    """Evidence coverage for one research question."""

    question: str
    status: Literal["SUPPORTED", "NO_SUPPORTING_EVIDENCE"]
    evidence: list[ResearchEvidence]


class ResearchExecutionResponse(BaseModel):
    """Structured result returned to the Java backend."""

    assignment_id: UUID
    execution_id: UUID

    status: Literal[
        "COMPLETED",
        "PARTIAL",
        "INSUFFICIENT_EVIDENCE",
    ]

    title: str
    summary: str

    findings: list[ResearchFinding]
    limitations: list[str]

    source_count: int = Field(ge=0)
    generated_at: datetime
