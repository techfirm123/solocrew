package com.solocrew.platform.audit;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class AuditEventWriterIntegrationTests {

    @Autowired
    private AuditEventWriter auditEventWriter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void appendsAuditEvent() {
        UUID eventId = UUID.randomUUID();

        AuditEvent event = new AuditEvent(
            eventId,
            Instant.now(),
            null,
            AuditActorType.SYSTEM,
            null,
            "FOUNDATION_TEST",
            "CREATE",
            "TEST_RESOURCE",
            UUID.randomUUID().toString(),
            AuditOutcome.SUCCESS,
            "audit-foundation-test",
            """
            {"source":"integration-test"}
            """
        );

        auditEventWriter.append(event);

        Integer eventCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM platform.audit_event
            WHERE event_id = ?
            """,
            Integer.class,
            eventId
        );

        assertEquals(1, eventCount);
    }

    @Test
    @Transactional
    void rejectsAuditEventUpdates() {
        UUID eventId = UUID.randomUUID();

        AuditEvent event = new AuditEvent(
            eventId,
            Instant.now(),
            null,
            AuditActorType.SYSTEM,
            null,
            "FOUNDATION_TEST",
            "CREATE",
            "TEST_RESOURCE",
            UUID.randomUUID().toString(),
            AuditOutcome.SUCCESS,
            "audit-foundation-test",
            "{}"
        );

        auditEventWriter.append(event);

        assertThrows(
            DataAccessException.class,
            () -> jdbcTemplate.update(
                """
                UPDATE platform.audit_event
                SET action = 'MODIFIED'
                WHERE event_id = ?
                """,
                eventId
            )
        );
    }
}
