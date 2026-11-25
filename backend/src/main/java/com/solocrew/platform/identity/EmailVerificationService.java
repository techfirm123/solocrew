package com.solocrew.platform.identity;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solocrew.platform.audit.AuditActorType;
import com.solocrew.platform.audit.AuditEvent;
import com.solocrew.platform.audit.AuditEventWriter;
import com.solocrew.platform.audit.AuditOutcome;
import com.solocrew.platform.email.EmailOutboxService;
import com.solocrew.platform.email.VerificationEmailTemplate;

@Service
public class EmailVerificationService {

    private static final int MAXIMUM_TOKENS_PER_15_MINUTES = 3;
    private static final int MAXIMUM_TOKENS_PER_DAY = 10;

    private final JdbcTemplate jdbcTemplate;
    private final EmailVerificationTokenService tokenService;
    private final VerificationEmailTemplate emailTemplate;
    private final EmailOutboxService emailOutboxService;
    private final AuditEventWriter auditEventWriter;
    private final Clock clock;

    public EmailVerificationService(
        JdbcTemplate jdbcTemplate,
        EmailVerificationTokenService tokenService,
        VerificationEmailTemplate emailTemplate,
        EmailOutboxService emailOutboxService,
        AuditEventWriter auditEventWriter,
        Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.tokenService = tokenService;
        this.emailTemplate = emailTemplate;
        this.emailOutboxService = emailOutboxService;
        this.auditEventWriter = auditEventWriter;
        this.clock = clock;
    }

    @Transactional
    public boolean verify(String rawToken) {
        String tokenHash = tokenService.hash(rawToken);

        List<VerificationTokenRecord> tokens = jdbcTemplate.query(
            """
            SELECT id, user_id
            FROM platform.email_verification_token
            WHERE token_hash = ?
              AND consumed_at IS NULL
              AND expires_at > CURRENT_TIMESTAMP
            FOR UPDATE
            """,
            (resultSet, rowNumber) -> new VerificationTokenRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("user_id", UUID.class)
            ),
            tokenHash
        );

        if (tokens.isEmpty()) {
            recordVerificationFailure();
            return false;
        }

        VerificationTokenRecord verificationToken = tokens.getFirst();

        Instant now = clock.instant();
        Timestamp timestamp = Timestamp.from(now);

        int activatedRows = jdbcTemplate.update(
            """
            UPDATE platform.user_account
            SET
                status = 'ACTIVE',
                email_verified_at = ?,
                updated_at = ?
            WHERE id = ?
              AND status = 'PENDING_EMAIL_VERIFICATION'
            """,
            timestamp,
            timestamp,
            verificationToken.userId()
        );

        if (activatedRows != 1) {
            consumeAllOutstandingTokens(
                verificationToken.userId(),
                timestamp
            );

            recordVerificationFailure();
            return false;
        }

        consumeAllOutstandingTokens(
            verificationToken.userId(),
            timestamp
        );

        UUID workspaceId = findOwnerWorkspace(
            verificationToken.userId()
        ).orElse(null);

        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                now,
                workspaceId,
                AuditActorType.USER,
                verificationToken.userId(),
                "EMAIL_VERIFICATION",
                "VERIFY",
                "USER_ACCOUNT",
                verificationToken.userId().toString(),
                AuditOutcome.SUCCESS,
                null,
                """
                {"accountStatus":"ACTIVE"}
                """
            )
        );

        return true;
    }

    @Transactional
    public void resend(String email) {
        String normalizedEmail = normalizeEmail(email);

        List<PendingUser> users = jdbcTemplate.query(
            """
            SELECT id, email
            FROM platform.user_account
            WHERE LOWER(BTRIM(email)) = ?
              AND status = 'PENDING_EMAIL_VERIFICATION'
            """,
            (resultSet, rowNumber) -> new PendingUser(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("email")
            ),
            normalizedEmail
        );

        if (users.isEmpty()) {
            return;
        }

        PendingUser user = users.getFirst();

        Instant now = clock.instant();

        Integer recentTokenCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM platform.email_verification_token
            WHERE user_id = ?
              AND created_at > ?
            """,
            Integer.class,
            user.userId(),
            Timestamp.from(now.minus(Duration.ofMinutes(15)))
        );

        Integer dailyTokenCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM platform.email_verification_token
            WHERE user_id = ?
              AND created_at > ?
            """,
            Integer.class,
            user.userId(),
            Timestamp.from(now.minus(Duration.ofDays(1)))
        );

        if (
            recentTokenCount == null
                || dailyTokenCount == null
                || recentTokenCount >= MAXIMUM_TOKENS_PER_15_MINUTES
                || dailyTokenCount >= MAXIMUM_TOKENS_PER_DAY
        ) {
            recordResendRateLimit(user.userId(), now);
            return;
        }

        Timestamp timestamp = Timestamp.from(now);

        consumeAllOutstandingTokens(user.userId(), timestamp);

        String rawToken = tokenService.issue(user.userId());

        emailOutboxService.enqueue(
            user.email(),
            emailTemplate.create(rawToken)
        );

        UUID workspaceId = findOwnerWorkspace(
            user.userId()
        ).orElse(null);

        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                now,
                workspaceId,
                AuditActorType.USER,
                user.userId(),
                "EMAIL_VERIFICATION_RESEND",
                "CREATE",
                "EMAIL_VERIFICATION_TOKEN",
                user.userId().toString(),
                AuditOutcome.SUCCESS,
                null,
                """
                {"deliveryRequested":true}
                """
            )
        );
    }

    private void consumeAllOutstandingTokens(
        UUID userId,
        Timestamp consumedAt
    ) {
        jdbcTemplate.update(
            """
            UPDATE platform.email_verification_token
            SET consumed_at = ?
            WHERE user_id = ?
              AND consumed_at IS NULL
            """,
            consumedAt,
            userId
        );
    }

    private Optional<UUID> findOwnerWorkspace(UUID userId) {
        List<UUID> workspaceIds = jdbcTemplate.query(
            """
            SELECT workspace_id
            FROM platform.workspace_membership
            WHERE user_id = ?
              AND role = 'OWNER'
              AND status = 'ACTIVE'
            ORDER BY created_at
            LIMIT 1
            """,
            (resultSet, rowNumber) ->
                resultSet.getObject("workspace_id", UUID.class),
            userId
        );

        return workspaceIds.stream().findFirst();
    }

    private void recordVerificationFailure() {
        Instant now = clock.instant();

        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                now,
                null,
                AuditActorType.SYSTEM,
                null,
                "EMAIL_VERIFICATION",
                "VERIFY",
                "EMAIL_VERIFICATION_TOKEN",
                null,
                AuditOutcome.FAILURE,
                null,
                """
                {"reason":"INVALID_OR_EXPIRED_TOKEN"}
                """
            )
        );
    }

    private void recordResendRateLimit(
        UUID userId,
        Instant now
    ) {
        UUID workspaceId = findOwnerWorkspace(userId)
            .orElse(null);

        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                now,
                workspaceId,
                AuditActorType.USER,
                userId,
                "EMAIL_VERIFICATION_RESEND",
                "CREATE",
                "EMAIL_VERIFICATION_TOKEN",
                userId.toString(),
                AuditOutcome.DENIED,
                null,
                """
                {"reason":"RATE_LIMITED"}
                """
            )
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record VerificationTokenRecord(
        UUID tokenId,
        UUID userId
    ) {
    }

    private record PendingUser(
        UUID userId,
        String email
    ) {
    }
}
