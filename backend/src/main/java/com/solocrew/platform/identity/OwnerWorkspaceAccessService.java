
package com.solocrew.platform.identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OwnerWorkspaceAccessService {

    private static final String FIND_ACTIVE_OWNER_CONTEXT = """
        SELECT
            account.id AS user_id,
            account.email,
            account.display_name,
            workspace.id AS workspace_id,
            workspace.name AS workspace_name,
            workspace.slug AS workspace_slug,
            membership.role AS membership_role
        FROM platform.user_account account
        JOIN platform.workspace_membership membership
            ON membership.user_id = account.id
           AND membership.status = 'ACTIVE'
        JOIN platform.business_workspace workspace
            ON workspace.id = membership.workspace_id
           AND workspace.status = 'ACTIVE'
        WHERE account.id = ?
          AND account.status = 'ACTIVE'
        ORDER BY
            CASE membership.role
                WHEN 'OWNER' THEN 0
                WHEN 'ADMIN' THEN 1
                ELSE 2
            END,
            membership.created_at,
            workspace.created_at
        LIMIT 1
        """;

    private final JdbcTemplate jdbcTemplate;

    public OwnerWorkspaceAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<OwnerSessionResponse> findActiveOwnerContext(
        UUID userId
    ) {
        List<OwnerSessionResponse> results = jdbcTemplate.query(
            FIND_ACTIVE_OWNER_CONTEXT,
            (resultSet, rowNumber) -> new OwnerSessionResponse(
                resultSet.getObject("user_id", UUID.class),
                resultSet.getString("email"),
                resultSet.getString("display_name"),
                new OwnerSessionResponse.Workspace(
                    resultSet.getObject("workspace_id", UUID.class),
                    resultSet.getString("workspace_name"),
                    resultSet.getString("workspace_slug"),
                    resultSet.getString("membership_role")
                )
            ),
            userId
        );

        return results.stream().findFirst();
    }
}
