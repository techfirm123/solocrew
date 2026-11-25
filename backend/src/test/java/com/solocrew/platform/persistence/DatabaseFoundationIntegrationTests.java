package com.solocrew.platform.persistence;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseFoundationIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void connectsToPostgresql() {
        String databaseVersion = jdbcTemplate.queryForObject(
            "SELECT version()",
            String.class
        );

        assertNotNull(databaseVersion);
        assertTrue(
            databaseVersion.startsWith("PostgreSQL"),
            "Expected PostgreSQL but received: " + databaseVersion
        );
    }

    @Test
    void connectsToDedicatedTestDatabase() {
        String databaseName = jdbcTemplate.queryForObject(
            "SELECT current_database()",
            String.class
        );

        assertEquals("solocrew_test", databaseName);
    }

    @Test
    void flywayAppliesInitialPlatformSchemaMigration() {
        Integer platformSchemaCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.schemata
            WHERE schema_name = 'platform'
            """,
            Integer.class
        );

        assertEquals(1, platformSchemaCount);

        Integer successfulMigrationCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM public.flyway_schema_history
            WHERE version = '1'
              AND success = TRUE
            """,
            Integer.class
        );

        assertEquals(1, successfulMigrationCount);
    }

    @Test
    void jdbcUsesSpringManagedDatabaseTransactions() {
        TransactionTemplate transactionTemplate =
            new TransactionTemplate(transactionManager);

        String databaseName = transactionTemplate.execute(status -> {
            assertTrue(
                TransactionSynchronizationManager
                    .isActualTransactionActive(),
                "Expected an active Spring-managed transaction"
            );

            return jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
            );
        });

        assertEquals("solocrew_test", databaseName);
    }
}
