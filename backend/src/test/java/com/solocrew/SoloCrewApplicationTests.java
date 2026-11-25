package com.solocrew;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class SoloCrewApplicationTests {

    private static final HttpClient HTTP_CLIENT =
        HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private Clock clock;

    @Test
    void applicationClockUsesUtc() {
        assertEquals(ZoneOffset.UTC, clock.getZone());
    }

    @Test
    void healthAndProbeEndpointsReturnUp()
        throws Exception {

        List<String> endpoints = List.of(
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness"
        );

        for (String endpoint : endpoints) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                    "http://localhost:" + port + endpoint
                ))
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(
                200,
                response.statusCode(),
                "Unexpected HTTP status for " + endpoint
            );

            assertTrue(
                response.body().contains("\"status\":\"UP\""),
                "Unexpected health response for " + endpoint
                    + ": " + response.body()
            );
        }
    }
}
