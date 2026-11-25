package com.solocrew.platform.email;

import java.util.Objects;

public record EmailContent(
    String subject,
    String body
) {

    public EmailContent {
        Objects.requireNonNull(subject, "subject is required");
        Objects.requireNonNull(body, "body is required");

        if (subject.isBlank()) {
            throw new IllegalArgumentException(
                "Email subject cannot be blank"
            );
        }
    }
}
