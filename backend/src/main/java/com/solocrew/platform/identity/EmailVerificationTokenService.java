package com.solocrew.platform.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationTokenService {

    private static final Duration TOKEN_LIFETIME =
        Duration.ofHours(24);

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public EmailVerificationTokenService(
        JdbcTemplate jdbcTemplate,
        Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
        this.secureRandom = new SecureRandom();
    }

    public String issue(UUID userId) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String rawToken = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tokenBytes);

        String tokenHash = hash(rawToken);

        var now = clock.instant();

        jdbcTemplate.update(
            """
            INSERT INTO platform.email_verification_token
            (
                id,
                user_id,
                token_hash,
                expires_at,
                created_at
            )
            VALUES (?, ?, ?, ?, ?)
            """,
            UUID.randomUUID(),
            userId,
            tokenHash,
            Timestamp.from(now.plus(TOKEN_LIFETIME)),
            Timestamp.from(now)
        );

        return rawToken;
    }

    public String hash(String rawToken) {
        try {
            MessageDigest messageDigest =
                MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = messageDigest.digest(
                rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is not available",
                exception
            );
        }
    }
}
