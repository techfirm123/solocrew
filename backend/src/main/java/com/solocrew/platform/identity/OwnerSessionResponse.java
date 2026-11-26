
package com.solocrew.platform.identity;

import java.util.UUID;

public record OwnerSessionResponse(
    UUID userId,
    String email,
    String displayName,
    Workspace workspace
) {

    public record Workspace(
        UUID id,
        String name,
        String slug,
        String role
    ) {
    }
}
