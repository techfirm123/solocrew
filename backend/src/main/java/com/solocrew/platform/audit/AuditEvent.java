package com.solocrew.platform.audit;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(
    UUID eventId,
    Instant occurredAt,
    UUID workspaceId,
    AuditActorType actorType,
    UUID actorId,
    String eventType,
    String action,
    String resourceType,
    String resourceId,
    AuditOutcome outcome,
    String correlationId,
    String metadataJson
) {

    public AuditEvent {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        Objects.requireNonNull(actorType, "actorType is required");
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(action, "action is required");
        Objects.requireNonNull(outcome, "outcome is required");

        if (eventType.isBlank()) {
            throw new IllegalArgumentException(
                "eventType cannot be blank"
            );
        }

        if (action.isBlank()) {
            throw new IllegalArgumentException(
                "action cannot be blank"
            );
        }

        if (metadataJson == null || metadataJson.isBlank()) {
            metadataJson = "{}";
        }
    }
}
