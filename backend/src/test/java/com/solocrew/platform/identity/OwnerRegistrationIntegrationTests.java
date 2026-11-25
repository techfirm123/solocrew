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
class OwnerRegistrationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registrationCreatesPendingOwnerAndWorkspace() throws Exception {
        String email = uniqueEmail();

        register(email);

        UserRecord user = findUser(email);

        assertEquals(
            UserAccountStatus.PENDING_EMAIL_VERIFICATION.name(),
            user.status()
        );

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.business_workspace
                WHERE created_by_user_id = ?
                """,
                user.id()
            )
        );

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.workspace_membership
                WHERE user_id = ?
                  AND role = 'OWNER'
                  AND status = 'ACTIVE'
                """,
                user.id()
            )
        );
    }

    @Test
    void registrationCreatesVerificationTokenAndOutboxMessage()
        throws Exception {

        String email = uniqueEmail();

        register(email);

        UserRecord user = findUser(email);

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.email_verification_token
                WHERE user_id = ?
                  AND consumed_at IS NULL
                """,
                user.id()
            )
        );

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.email_outbox
                WHERE recipient_email = ?
                  AND status = 'PENDING'
                """,
                email
            )
        );
    }

    @Test
    void registrationStoresOnlyTokenHash() throws Exception {
        String email = uniqueEmail();

        register(email);

        String tokenHash = jdbcTemplate.queryForObject(
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

        assertNotNull(tokenHash);
        assertEquals(64, tokenHash.length());
    }

    @Test
    void registrationWritesAuditEvent() throws Exception {
        String email = uniqueEmail();

        register(email);

        UserRecord user = findUser(email);

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.audit_event
                WHERE actor_id = ?
                  AND event_type = 'OWNER_REGISTRATION'
                  AND outcome = 'SUCCESS'
                """,
                user.id()
            )
        );
    }

    @Test
    void duplicateRegistrationDoesNotCreateAnotherAccount()
        throws Exception {

        String email = uniqueEmail();

        register(email);
        register(email);

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.user_account
                WHERE email = ?
                """,
                email
            )
        );

        assertEquals(
            1,
            count(
                """
                SELECT COUNT(*)
                FROM platform.email_outbox
                WHERE recipient_email = ?
                """,
                email
            )
        );
    }

    private void register(String email) throws Exception {
        String request = """
            {
                "email": "%s",
                "displayName": "Foundation Test Owner",
                "password": "test-owner-password-123",
                "businessName": "Foundation Test Business"
            }
            """.formatted(email);

        mockMvc.perform(
                post("/api/v1/public/registrations")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isAccepted());
    }

    private UserRecord findUser(String email) {
        return jdbcTemplate.queryForObject(
            """
            SELECT id, status
            FROM platform.user_account
            WHERE email = ?
            """,
            (resultSet, rowNumber) -> new UserRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("status")
            ),
            email
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

    private String uniqueEmail() {
        return "owner-" + UUID.randomUUID()
            + "@example.test";
    }

    private record UserRecord(
        UUID id,
        String status
    ) {
    }
}
