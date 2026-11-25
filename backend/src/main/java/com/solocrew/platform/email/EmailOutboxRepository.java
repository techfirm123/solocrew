package com.solocrew.platform.email;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EmailOutboxRepository {

    private static final RowMapper<EmailOutboxDelivery> DELIVERY_MAPPER =
        EmailOutboxRepository::mapDelivery;

    private static final String CLAIM_NEXT_EMAIL = """
        WITH next_email AS (
            SELECT id
            FROM platform.email_outbox
            WHERE
                (
                    status IN ('PENDING', 'RETRY')
                    AND available_at <= CURRENT_TIMESTAMP
                )
                OR
                (
                    status = 'PROCESSING'
                    AND locked_at <
                        CURRENT_TIMESTAMP - INTERVAL '5 minutes'
                )
            ORDER BY available_at ASC, created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
        )
        UPDATE platform.email_outbox AS email
        SET
            status = 'PROCESSING',
            attempts = email.attempts + 1,
            locked_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        FROM next_email
        WHERE email.id = next_email.id
        RETURNING
            email.id,
            email.recipient_email,
            email.attempts,
            email.payload_nonce,
            email.payload_ciphertext
        """;

    private final JdbcTemplate jdbcTemplate;

    public EmailOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void enqueue(
        UUID id,
        String recipientEmail,
        EncryptedEmailPayload payload,
        Instant createdAt
    ) {
        Timestamp timestamp = Timestamp.from(createdAt);

        jdbcTemplate.update(
            """
            INSERT INTO platform.email_outbox
            (
                id,
                recipient_email,
                payload_nonce,
                payload_ciphertext,
                status,
                attempts,
                available_at,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, 'PENDING', 0, ?, ?, ?)
            """,
            id,
            recipientEmail,
            payload.nonce(),
            payload.ciphertext(),
            timestamp,
            timestamp,
            timestamp
        );
    }

    public Optional<EmailOutboxDelivery> claimNext() {
        List<EmailOutboxDelivery> deliveries = jdbcTemplate.query(
            CLAIM_NEXT_EMAIL,
            DELIVERY_MAPPER
        );

        return deliveries.stream().findFirst();
    }

    public void markSent(UUID id) {
        jdbcTemplate.update(
            """
            UPDATE platform.email_outbox
            SET
                status = 'SENT',
                sent_at = CURRENT_TIMESTAMP,
                locked_at = NULL,
                payload_nonce = NULL,
                payload_ciphertext = NULL,
                last_error_code = NULL,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND status = 'PROCESSING'
            """,
            id
        );
    }

    public void markFailed(
        UUID id,
        String errorCode,
        long retryDelaySeconds
    ) {
        jdbcTemplate.update(
            """
            UPDATE platform.email_outbox
            SET
                status = CASE
                    WHEN attempts >= 5 THEN 'DEAD'
                    ELSE 'RETRY'
                END,
                available_at = CURRENT_TIMESTAMP
                    + (? * INTERVAL '1 second'),
                locked_at = NULL,
                last_error_code = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND status = 'PROCESSING'
            """,
            retryDelaySeconds,
            errorCode,
            id
        );
    }

    private static EmailOutboxDelivery mapDelivery(
        ResultSet resultSet,
        int rowNumber
    ) throws SQLException {

        return new EmailOutboxDelivery(
            resultSet.getObject("id", UUID.class),
            resultSet.getString("recipient_email"),
            resultSet.getInt("attempts"),
            resultSet.getBytes("payload_nonce"),
            resultSet.getBytes("payload_ciphertext")
        );
    }
}
