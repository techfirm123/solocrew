package com.solocrew.platform.identity;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class OwnerUserDetailsServiceIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OwnerUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void loadsUserByNormalizedEmail() {
        UUID userId = UUID.randomUUID();

        String rawPassword = "temporary-test-password";

        String encodedPassword = passwordEncoder.encode(
            rawPassword
        );

        jdbcTemplate.update(
            """
            INSERT INTO platform.user_account
            (
                id,
                email,
                display_name,
                password_hash,
                status
            )
            VALUES (?, ?, ?, ?, ?)
            """,
            userId,
            "foundation.test@example.com",
            "Foundation Test User",
            encodedPassword,
            UserAccountStatus.PENDING_EMAIL_VERIFICATION.name()
        );

        UserDetails userDetails =
            userDetailsService.loadUserByUsername(
                " FOUNDATION.TEST@EXAMPLE.COM "
            );

        assertEquals(
            userId,
            ((OwnerUserPrincipal) userDetails).userId()
        );

        assertEquals(
            "foundation.test@example.com",
            userDetails.getUsername()
        );

        assertFalse(
            userDetails.isEnabled(),
            "Unverified accounts must not be enabled"
        );

        assertTrue(
            passwordEncoder.matches(
                rawPassword,
                userDetails.getPassword()
            )
        );

        assertFalse(
            encodedPassword.equals(rawPassword),
            "Password storage must not contain plaintext"
        );
    }
}
