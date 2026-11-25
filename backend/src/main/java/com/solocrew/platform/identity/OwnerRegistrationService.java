package com.solocrew.platform.identity;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solocrew.platform.audit.AuditActorType;
import com.solocrew.platform.audit.AuditEvent;
import com.solocrew.platform.audit.AuditEventWriter;
import com.solocrew.platform.audit.AuditOutcome;
import com.solocrew.platform.email.EmailOutboxService;
import com.solocrew.platform.email.VerificationEmailTemplate;

@Service
public class OwnerRegistrationService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService tokenService;
    private final VerificationEmailTemplate emailTemplate;
    private final EmailOutboxService emailOutboxService;
    private final AuditEventWriter auditEventWriter;
    private final Clock clock;

    public OwnerRegistrationService(
        JdbcTemplate jdbcTemplate,
        PasswordEncoder passwordEncoder,
        EmailVerificationTokenService tokenService,
        VerificationEmailTemplate emailTemplate,
        EmailOutboxService emailOutboxService,
        AuditEventWriter auditEventWriter,
        Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailTemplate = emailTemplate;
        this.emailOutboxService = emailOutboxService;
        this.auditEventWriter = auditEventWriter;
        this.clock = clock;
    }

    @Transactional
    public void register(OwnerRegistrationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        Boolean accountExists = jdbcTemplate.queryForObject(
            """
            SELECT EXISTS (
                SELECT 1
                FROM platform.user_account
                WHERE LOWER(BTRIM(email)) = ?
            )
            """,
            Boolean.class,
            normalizedEmail
        );

        if (Boolean.TRUE.equals(accountExists)) {
            return;
        }

        if (
            request.password().getBytes(StandardCharsets.UTF_8).length > 72
        ) {
            throw new IllegalArgumentException(
                "Password exceeds the supported length"
            );
        }

        Instant now = clock.instant();
        Timestamp timestamp = Timestamp.from(now);

        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        String passwordHash = passwordEncoder.encode(
            request.password()
        );

        jdbcTemplate.update(
            """
            INSERT INTO platform.user_account
            (
                id,
                email,
                display_name,
                password_hash,
                status,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            userId,
            normalizedEmail,
            request.displayName().trim(),
            passwordHash,
            UserAccountStatus.PENDING_EMAIL_VERIFICATION.name(),
            timestamp,
            timestamp
        );

        String workspaceSlug = createWorkspaceSlug(
            request.businessName()
        );

        jdbcTemplate.update(
            """
            INSERT INTO platform.business_workspace
            (
                id,
                name,
                slug,
                status,
                created_by_user_id,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?)
            """,
            workspaceId,
            request.businessName().trim(),
            workspaceSlug,
            userId,
            timestamp,
            timestamp
        );

        jdbcTemplate.update(
            """
            INSERT INTO platform.workspace_membership
            (
                id,
                workspace_id,
                user_id,
                role,
                status,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, 'OWNER', 'ACTIVE', ?, ?)
            """,
            membershipId,
            workspaceId,
            userId,
            timestamp,
            timestamp
        );

        String rawToken = tokenService.issue(userId);

        emailOutboxService.enqueue(
            normalizedEmail,
            emailTemplate.create(rawToken)
        );

        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                now,
                workspaceId,
                AuditActorType.USER,
                userId,
                "OWNER_REGISTRATION",
                "CREATE",
                "USER_ACCOUNT",
                userId.toString(),
                AuditOutcome.SUCCESS,
                null,
                """
                {"accountStatus":"PENDING_EMAIL_VERIFICATION"}
                """
            )
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String createWorkspaceSlug(String businessName) {
        String normalized = businessName
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");

        if (normalized.isBlank()) {
            normalized = "business";
        }

        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }

        normalized = normalized.replaceAll("-+$", "");

        String suffix = UUID.randomUUID()
            .toString()
            .substring(0, 8);

        return normalized + "-" + suffix;
    }
}
