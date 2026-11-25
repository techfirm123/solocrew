package com.solocrew.platform.identity;

import java.util.Locale;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OwnerUserDetailsService implements UserDetailsService {

    private static final String FIND_USER_BY_EMAIL = """
        SELECT
            id,
            email,
            password_hash,
            status
        FROM platform.user_account
        WHERE LOWER(BTRIM(email)) = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public OwnerUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            throw new UsernameNotFoundException(
                "User account was not found"
            );
        }

        try {
            return jdbcTemplate.queryForObject(
                FIND_USER_BY_EMAIL,
                (resultSet, rowNumber) -> new OwnerUserPrincipal(
                    resultSet.getObject("id", java.util.UUID.class),
                    resultSet.getString("email"),
                    resultSet.getString("password_hash"),
                    UserAccountStatus.valueOf(
                        resultSet.getString("status")
                    )
                ),
                normalizedEmail
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new UsernameNotFoundException(
                "User account was not found",
                exception
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
