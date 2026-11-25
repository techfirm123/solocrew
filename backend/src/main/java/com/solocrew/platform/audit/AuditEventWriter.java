package com.solocrew.platform.audit;

import java.time.ZoneOffset;

import org.slf4j.MDC;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solocrew.platform.observability.CorrelationIdFilter;

@Service
public class AuditEventWriter {

    private static final String INSERT_AUDIT_EVENT = """
        INSERT INTO platform.audit_event
        (
            event_id,
            occurred_at,
            workspace_id,
            actor_type,
            actor_id,
            event_type,
            action,
            resource_type,
            resource_id,
            outcome,
            correlation_id,
            metadata
        )
        VALUES
        (
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            ?,
            CAST(? AS JSONB)
        )
        """;

    private final JdbcTemplate jdbcTemplate;

    public AuditEventWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void append(AuditEvent event) {
        String correlationId = event.correlationId();

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = MDC.get(
                CorrelationIdFilter.MDC_KEY
            );
        }

        jdbcTemplate.update(
            INSERT_AUDIT_EVENT,
            event.eventId(),
            event.occurredAt().atOffset(ZoneOffset.UTC),
            event.workspaceId(),
            event.actorType().name(),
            event.actorId(),
            event.eventType(),
            event.action(),
            event.resourceType(),
            event.resourceId(),
            event.outcome().name(),
            correlationId,
            event.metadataJson()
        );
    }
}
