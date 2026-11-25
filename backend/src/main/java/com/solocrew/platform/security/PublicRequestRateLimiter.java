package com.solocrew.platform.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicRequestRateLimiter {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public PublicRequestRateLimiter(
        JdbcTemplate jdbcTemplate,
        Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional
    public boolean allow(
        String action,
        String remoteAddress,
        Duration window,
        int maximumRequests
    ) {
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException(
                "Rate-limit window must be positive"
            );
        }

        if (maximumRequests < 1) {
            throw new IllegalArgumentException(
                "Maximum requests must be positive"
            );
        }

        long windowSeconds = window.toSeconds();

        if (windowSeconds < 1) {
            throw new IllegalArgumentException(
                "Rate-limit window must be at least one second"
            );
        }

        Instant now = clock.instant();

        long windowStartEpoch =
            Math.floorDiv(now.getEpochSecond(), windowSeconds)
                * windowSeconds;

        Instant windowStart = Instant.ofEpochSecond(
            windowStartEpoch
        );

        String fingerprintHash = fingerprint(
            action,
            remoteAddress
        );

        Integer requestCount = jdbcTemplate.queryForObject(
            """
            INSERT INTO platform.public_request_rate_limit
            (
                action,
                fingerprint_hash,
                window_started_at,
                request_count,
                updated_at
            )
            VALUES (?, ?, ?, 1, ?)
            ON CONFLICT (
                action,
                fingerprint_hash,
                window_started_at
            )
            DO UPDATE SET
                request_count =
                    platform.public_request_rate_limit.request_count + 1,
                updated_at = EXCLUDED.updated_at
            RETURNING request_count
            """,
            Integer.class,
            action,
            fingerprintHash,
            Timestamp.from(windowStart),
            Timestamp.from(now)
        );

        jdbcTemplate.update(
            """
            DELETE FROM platform.public_request_rate_limit
            WHERE window_started_at < ?
            """,
            Timestamp.from(now.minus(Duration.ofDays(2)))
        );

        return requestCount != null
            && requestCount <= maximumRequests;
    }

    private String fingerprint(
        String action,
        String remoteAddress
    ) {
        String source = remoteAddress == null
            ? "unknown"
            : remoteAddress;

        try {
            MessageDigest messageDigest =
                MessageDigest.getInstance("SHA-256");

            byte[] digest = messageDigest.digest(
                (action + "\u0000" + source)
                    .getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is not available",
                exception
            );
        }
    }
}
