package com.solocrew.platform.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFoundationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/actuator/health")
            )
            .andExpect(status().isOk());
    }

    @Test
    void protectedApiRequiresAuthentication() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/v1/private/probe"
                )
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingRequestsRequireCsrfToken() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/actuator/health")
                    .with(user("owner@example.com").roles("USER"))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void validCsrfTokenPassesSecurityFilter() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/actuator/health")
                    .with(user("owner@example.com").roles("USER"))
                    .with(csrf())
            )
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void correlationIdIsReturnedInResponse() throws Exception {
        String correlationId = "foundation-test-005-a";

        mockMvc.perform(
                MockMvcRequestBuilders.get("/actuator/health")
                    .header("X-Correlation-ID", correlationId)
            )
            .andExpect(status().isOk())
            .andExpect(
                header().string(
                    "X-Correlation-ID",
                    correlationId
                )
            );
    }
}
