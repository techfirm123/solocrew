package com.solocrew.platform.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SessionPersistenceIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void springSessionRepositoryFilterIsConfigured() {
        assertTrue(
            applicationContext.containsBean(
                "springSessionRepositoryFilter"
            ),
            "Spring Session JDBC repository filter must exist"
        );
    }

    @Test
    void flywayCreatedSpringSessionTables() {
        Integer sessionTableCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name IN (
                  'spring_session',
                  'spring_session_attributes'
              )
            """,
            Integer.class
        );

        assertEquals(
            2,
            sessionTableCount,
            "Expected both Spring Session JDBC tables"
        );
    }
}
