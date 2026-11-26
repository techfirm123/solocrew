
package com.solocrew.platform.identity;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OwnerAuthenticationIntegrationTests {

    private static final String RAW_PASSWORD =
        "Valid-Owner-Password-2026!";

    private static final String SESSION_COOKIE_NAME =
        "SOLOCREW_SESSION";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JsonMapper objectMapper;

    private TestOwner testOwner;


    @AfterEach
    void cleanUp() {
        if (testOwner == null) {
            return;
        }

        jdbcTemplate.update(
            """
            DELETE FROM public.spring_session
            WHERE principal_name = ?
            """,
            testOwner.email()
        );

        jdbcTemplate.update(
            """
            DELETE FROM platform.workspace_membership
            WHERE user_id = ?
            """,
            testOwner.userId()
        );

        jdbcTemplate.update(
            """
            DELETE FROM platform.business_workspace
            WHERE id = ?
            """,
            testOwner.workspaceId()
        );

        jdbcTemplate.update(
            """
            DELETE FROM platform.user_account
            WHERE id = ?
            """,
            testOwner.userId()
        );

        jdbcTemplate.update(
            """
            DELETE FROM platform.public_request_rate_limit
            WHERE action IN (
                'OWNER_LOGIN_IP',
                'OWNER_LOGIN_ACCOUNT'
            )
            """
        );
    }


    @Test
    void activeOwnerCanLoginAndAccessCurrentOwnerContext()
        throws Exception {

        testOwner = createOwner(UserAccountStatus.ACTIVE);

        CsrfState initialCsrf = fetchCsrf(null);

        MvcResult loginResult = mockMvc.perform(
                post("/api/v1/public/auth/login")
                    .cookie(initialCsrf.sessionCookie())
                    .header("X-CSRF-TOKEN", initialCsrf.token())
                    .contentType(APPLICATION_JSON)
                    .content(loginJson(
                        testOwner.email(),
                        RAW_PASSWORD
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.message")
                    .value("Signed in successfully.")
            )
            .andReturn();

        Cookie authenticatedSessionCookie =
            sessionCookieFrom(
                loginResult,
                initialCsrf.sessionCookie()
            );

        assertNotNull(authenticatedSessionCookie);

        assertNotEquals(
            initialCsrf.sessionCookie().getValue(),
            authenticatedSessionCookie.getValue(),
            "The session ID must change after authentication"
        );

        mockMvc.perform(
                get("/api/v1/owner/me")
                    .cookie(authenticatedSessionCookie)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.userId")
                    .value(testOwner.userId().toString())
            )
            .andExpect(
                jsonPath("$.email")
                    .value(testOwner.email())
            )
            .andExpect(
                jsonPath("$.displayName")
                    .value("Integration Test Owner")
            )
            .andExpect(
                jsonPath("$.workspace.id")
                    .value(testOwner.workspaceId().toString())
            )
            .andExpect(
                jsonPath("$.workspace.slug")
                    .value(testOwner.workspaceSlug())
            )
            .andExpect(
                jsonPath("$.workspace.role")
                    .value("OWNER")
            );

        Integer persistedSessionCount =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM public.spring_session
                WHERE principal_name = ?
                """,
                Integer.class,
                testOwner.email()
            );

        assertEquals(
            1,
            persistedSessionCount,
            "The authenticated session must be persisted in PostgreSQL"
        );

        Integer successfulLoginAuditCount =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM platform.audit_event
                WHERE actor_id = ?
                  AND event_type = 'OWNER_LOGIN'
                  AND outcome = 'SUCCESS'
                """,
                Integer.class,
                testOwner.userId()
            );

        assertEquals(1, successfulLoginAuditCount);
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        testOwner = createOwner(UserAccountStatus.ACTIVE);

        CsrfState csrf = fetchCsrf(null);

        mockMvc.perform(
                post("/api/v1/public/auth/login")
                    .cookie(csrf.sessionCookie())
                    .header("X-CSRF-TOKEN", csrf.token())
                    .contentType(APPLICATION_JSON)
                    .content(loginJson(
                        testOwner.email(),
                        "Incorrect-Owner-Password!"
                    ))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonPath("$.message")
                    .value("Email or password is incorrect.")
            );

        mockMvc.perform(
                get("/api/v1/owner/me")
                    .cookie(csrf.sessionCookie())
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsUnverifiedAccount() throws Exception {
        testOwner = createOwner(
            UserAccountStatus.PENDING_EMAIL_VERIFICATION
        );

        CsrfState csrf = fetchCsrf(null);

        mockMvc.perform(
                post("/api/v1/public/auth/login")
                    .cookie(csrf.sessionCookie())
                    .header("X-CSRF-TOKEN", csrf.token())
                    .contentType(APPLICATION_JSON)
                    .content(loginJson(
                        testOwner.email(),
                        RAW_PASSWORD
                    ))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonPath("$.message")
                    .value("Email or password is incorrect.")
            );

        mockMvc.perform(
                get("/api/v1/owner/me")
                    .cookie(csrf.sessionCookie())
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRequiresCsrfToken() throws Exception {
        testOwner = createOwner(UserAccountStatus.ACTIVE);

        mockMvc.perform(
                post("/api/v1/public/auth/login")
                    .contentType(APPLICATION_JSON)
                    .content(loginJson(
                        testOwner.email(),
                        RAW_PASSWORD
                    ))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void logoutInvalidatesAuthenticatedSession() throws Exception {
        testOwner = createOwner(UserAccountStatus.ACTIVE);

        CsrfState initialCsrf = fetchCsrf(null);

        MvcResult loginResult = mockMvc.perform(
                post("/api/v1/public/auth/login")
                    .cookie(initialCsrf.sessionCookie())
                    .header("X-CSRF-TOKEN", initialCsrf.token())
                    .contentType(APPLICATION_JSON)
                    .content(loginJson(
                        testOwner.email(),
                        RAW_PASSWORD
                    ))
            )
            .andExpect(status().isOk())
            .andReturn();

        Cookie authenticatedSessionCookie =
            sessionCookieFrom(
                loginResult,
                initialCsrf.sessionCookie()
            );

        CsrfState authenticatedCsrf =
            fetchCsrf(authenticatedSessionCookie);

        Cookie currentSessionCookie =
            authenticatedCsrf.sessionCookie();

        mockMvc.perform(
                post("/api/v1/owner/logout")
                    .cookie(currentSessionCookie)
                    .header(
                        "X-CSRF-TOKEN",
                        authenticatedCsrf.token()
                    )
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/v1/owner/me")
                    .cookie(authenticatedSessionCookie)
            )
            .andExpect(status().isUnauthorized());

        Integer persistedSessionCount =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM public.spring_session
                WHERE principal_name = ?
                """,
                Integer.class,
                testOwner.email()
            );

        assertEquals(
            0,
            persistedSessionCount,
            "Logout must remove the persisted session"
        );

        Integer logoutAuditCount =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM platform.audit_event
                WHERE actor_id = ?
                  AND event_type = 'OWNER_LOGOUT'
                  AND outcome = 'SUCCESS'
                """,
                Integer.class,
                testOwner.userId()
            );

        assertEquals(1, logoutAuditCount);
    }

    @Test
    void ownerPrincipalErasesPasswordHashAfterAuthentication() {
        OwnerUserPrincipal principal = new OwnerUserPrincipal(
            UUID.randomUUID(),
            "principal-test@example.com",
            "bcrypt-test-hash",
            UserAccountStatus.ACTIVE
        );

        assertEquals(
            "bcrypt-test-hash",
            principal.getPassword()
        );

        principal.eraseCredentials();

        assertNull(principal.getPassword());
    }

    private TestOwner createOwner(UserAccountStatus status) {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        String email = "auth-" + userId + "@example.com";
        String workspaceSlug =
            "auth-test-" + userId.toString().substring(0, 8);

        Timestamp verifiedAt = status == UserAccountStatus.ACTIVE
            ? Timestamp.from(Instant.now())
            : null;

        String passwordHash = passwordEncoder.encode(RAW_PASSWORD);

        jdbcTemplate.update(
            """
            INSERT INTO platform.user_account
            (
                id,
                email,
                display_name,
                password_hash,
                status,
                email_verified_at,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
            userId,
            email,
            "Integration Test Owner",
            passwordHash,
            status.name(),
            verifiedAt
        );

        jdbcTemplate.update(
            """
            INSERT INTO platform.business_workspace
            (
                id,
                name,
                slug,
                status,
                created_by_user_id
            )
            VALUES (?, ?, ?, 'ACTIVE', ?)
            """,
            workspaceId,
            "Authentication Test Workspace",
            workspaceSlug,
            userId
        );

        jdbcTemplate.update(
            """
            INSERT INTO platform.workspace_membership
            (
                id,
                workspace_id,
                user_id,
                role,
                status
            )
            VALUES (?, ?, ?, 'OWNER', 'ACTIVE')
            """,
            membershipId,
            workspaceId,
            userId
        );

        testOwner = new TestOwner(
            userId,
            workspaceId,
            email,
            workspaceSlug
        );

        return testOwner;
    }

    private CsrfState fetchCsrf(Cookie existingSessionCookie)
        throws Exception {

        MockHttpServletRequestBuilder request =
            get("/api/v1/public/csrf");

        if (existingSessionCookie != null) {
            request.cookie(existingSessionCookie);
        }

        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        String headerName = body.get("headerName").asText();

        assertEquals("X-CSRF-TOKEN", headerName);

        Cookie sessionCookie = sessionCookieFrom(
            result,
            existingSessionCookie
        );

        assertNotNull(sessionCookie);

        return new CsrfState(
            body.get("token").asText(),
            sessionCookie
        );
    }

    private Cookie sessionCookieFrom(
        MvcResult result,
        Cookie fallback
    ) {
        Cookie responseCookie =
            result.getResponse().getCookie(SESSION_COOKIE_NAME);

        if (responseCookie != null
            && responseCookie.getValue() != null
            && !responseCookie.getValue().isBlank()) {
            return responseCookie;
        }

        return fallback;
    }

    private String loginJson(
        String email,
        String password
    ) throws Exception {
        return objectMapper.writeValueAsString(
            new OwnerLoginRequest(email, password)
        );
    }

    private record CsrfState(
        String token,
        Cookie sessionCookie
    ) {
    }

    private record TestOwner(
        UUID userId,
        UUID workspaceId,
        String email,
        String workspaceSlug
    ) {
    }
}
