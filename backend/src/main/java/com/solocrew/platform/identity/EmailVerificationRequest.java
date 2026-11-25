package com.solocrew.platform.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationRequest(

    @NotBlank
    @Pattern(
        regexp = "^[A-Za-z0-9_-]{43}$",
        message = "Invalid verification token format"
    )
    String token

) {
}
