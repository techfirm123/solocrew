from uuid import uuid4

from fastapi.testclient import TestClient

from solocrew_runtime.api.main import app


def build_payload() -> dict[str, object]:
    return {
        "assignment_id": str(uuid4()),
        "execution_id": str(uuid4()),
        "topic": "Laptop battery health",
        "research_questions": [
            "How does battery age affect battery capacity?",
        ],
        "sources": [
            {
                "source_id": "source-001",
                "title": "Battery maintenance notes",
                "source_url": None,
                "excerpt": (
                    "As lithium-ion batteries age, their maximum charge capacity "
                    "gradually decreases."
                ),
            }
        ],
    }


def test_liveness_does_not_require_internal_token(monkeypatch) -> None:
    monkeypatch.delenv("SOLOCREW_INTERNAL_TOKEN", raising=False)

    with TestClient(app) as client:
        response = client.get("/health/liveness")

    assert response.status_code == 200
    assert response.json()["status"] == "UP"


def test_readiness_requires_internal_token_configuration(monkeypatch) -> None:
    monkeypatch.delenv("SOLOCREW_INTERNAL_TOKEN", raising=False)

    with TestClient(app) as client:
        response = client.get("/health/readiness")

    assert response.status_code == 503


def test_research_endpoint_rejects_missing_token(monkeypatch) -> None:
    monkeypatch.setenv(
        "SOLOCREW_INTERNAL_TOKEN",
        "local-test-secret",
    )

    with TestClient(app) as client:
        response = client.post(
            "/internal/v1/research/execute",
            json=build_payload(),
        )

    assert response.status_code == 401


def test_research_endpoint_rejects_invalid_token(monkeypatch) -> None:
    monkeypatch.setenv(
        "SOLOCREW_INTERNAL_TOKEN",
        "local-test-secret",
    )

    with TestClient(app) as client:
        response = client.post(
            "/internal/v1/research/execute",
            headers={
                "Authorization": "Bearer wrong-secret",
            },
            json=build_payload(),
        )

    assert response.status_code == 403


def test_research_endpoint_executes_with_valid_token(monkeypatch) -> None:
    monkeypatch.setenv(
        "SOLOCREW_INTERNAL_TOKEN",
        "local-test-secret",
    )

    with TestClient(app) as client:
        response = client.post(
            "/internal/v1/research/execute",
            headers={
                "Authorization": "Bearer local-test-secret",
            },
            json=build_payload(),
        )

    assert response.status_code == 200

    result = response.json()

    assert result["status"] == "COMPLETED"
    assert result["source_count"] == 1
    assert result["findings"][0]["status"] == "SUPPORTED"
    assert result["findings"][0]["evidence"][0]["source_id"] == "source-001"


def test_research_endpoint_rejects_unknown_request_fields(monkeypatch) -> None:
    monkeypatch.setenv(
        "SOLOCREW_INTERNAL_TOKEN",
        "local-test-secret",
    )

    payload = build_payload()
    payload["unexpected_field"] = "should be rejected"

    with TestClient(app) as client:
        response = client.post(
            "/internal/v1/research/execute",
            headers={
                "Authorization": "Bearer local-test-secret",
            },
            json=payload,
        )

    assert response.status_code == 422
