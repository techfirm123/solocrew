package com.solocrew.platform.identity;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.solocrew.platform.email.EmailContent;
import com.solocrew.platform.email.EmailPayloadCipher;
import com.solocrew.platform.email.EncryptedEmailPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmailVerificationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailPayloadCipher payloadCipher;

    @Test
    void validTokenActivatesAccount() throws Exception {
        String email = registerOwner();

        String token = retrieveVerificationToken(email);

        verify(token)
            .andExpect(status().isOk());

        String accountStatus = jdbcTemplate.queryForObject(
            """
            SELECT status
            FROM platform.user_account
            WHERE email = ?
            """,
            String.class,
            email
        );

        assertEquals(
            UserAccountStatus.ACTIVE.name(),
            accountStatus
        );

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.user_account
                WHERE email = ?
                  AND email_verified_at IS NOT NULL
                """,
                email
            )
        );

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.email_verification_token
                WHERE token_hash = (
                    SELECT token_hash
                    FROM platform.email_verification_token token
                    JOIN platform.user_account account
                      ON account.id = token.user_id
                    WHERE account.email = ?
                    LIMIT 1
                )
                  AND consumed_at IS NOT NULL
                """,
                email
            )
        );
    }

    @Test
    void verificationTokenCannotBeUsedTwice() throws Exception {
        String email = registerOwner();

        String token = retrieveVerificationToken(email);

        verify(token)
            .andExpect(status().isOk());

        verify(token)
            .andExpect(status().isBadRequest());
    }

    @Test
    void expiredVerificationTokenIsRejected() throws Exception {
        String email = registerOwner();

        String token = retrieveVerificationToken(email);

        String tokenHash = hashFromEmail(email);

        jdbcTemplate.update(
            """
            UPDATE platform.email_verification_token
            SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 minute'
            WHERE token_hash = ?
            """,
            tokenHash
        );

        verify(token)
            .andExpect(status().isBadRequest());

        String status = jdbcTemplate.queryForObject(
            """
            SELECT status
            FROM platform.user_account
            WHERE email = ?
            """,
            String.class,
            email
        );

        assertEquals(
            UserAccountStatus.PENDING_EMAIL_VERIFICATION.name(),
            status
        );
    }

    @Test
    void resendCreatesNewTokenAndInvalidatesPreviousToken()
        throws Exception {

        String email = registerOwner();

        String originalToken = retrieveVerificationToken(email);

        mockMvc.perform(
                post("/api/v1/public/email-verifications/resend")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s"}
                        """.formatted(email)
                    )
            )
            .andExpect(status().isAccepted());

        assertEquals(
            2,
            count(
                """
                SELECT COUNT(*)
                FROM platform.email_outbox
                WHERE recipient_email = ?
                """,
                email
            )
        );

        verify(originalToken)
            .andExpect(status().isBadRequest());
    }

    private String registerOwner() throws Exception {
        String email = "verify-" + UUID.randomUUID()
            + "@example.test";

        String request = """
            {
                "email": "%s",
                "displayName": "Verification Test Owner",
                "password": "test-owner-password-123",
                "businessName": "Verification Test Business"
            }
            """.formatted(email);

        mockMvc.perform(
                post("/api/v1/public/registrations")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isAccepted());

        return email;
    }

    private String retrieveVerificationToken(String email) {
        EncryptedEmailPayload encryptedPayload =
            jdbcTemplate.queryForObject(
                """
                SELECT payload_nonce, payload_ciphertext
                FROM platform.email_outbox
                WHERE recipient_email = ?
                ORDER BY created_at DESC
                LIMIT 1
                """,
                (resultSet, rowNumber) ->
                    new EncryptedEmailPayload(
                        resultSet.getBytes("payload_nonce"),
                        resultSet.getBytes("payload_ciphertext")
                    ),
                email
            );

        assertNotNull(encryptedPayload);

        EmailContent content = payloadCipher.decrypt(
            encryptedPayload
        );

        String marker = "token=";

        int tokenStart = content.body().indexOf(marker);

        assertTrue(
            tokenStart >= 0,
            "Verification URL must be present in the email"
        );

        String token = content.body()
            .substring(tokenStart + marker.length())
            .strip()
            .split("\\s+", 2)[0];

        assertEquals(43, token.length());

        return token;
    }

    private String hashFromEmail(String email) {
        return jdbcTemplate.queryForObject(
            """
            SELECT token_hash
            FROM platform.email_verification_token token
            JOIN platform.user_account account
              ON account.id = token.user_id
            WHERE account.email = ?
            """,
            String.class,
            email
        );
    }

    private org.springframework.test.web.servlet.ResultActions verify(
        String token
    ) throws Exception {

        return mockMvc.perform(
            post("/api/v1/public/email-verifications")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"token":"%s"}
                    """.formatted(token)
                )
        );
    }

    private int count(String sql, Object parameter) {
        Integer result = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            parameter
        );

        assertNotNull(result);

        return result;
    }
}
