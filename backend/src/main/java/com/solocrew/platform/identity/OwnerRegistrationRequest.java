package com.solocrew.platform.identity;

import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerRegistrationRequest(

    @NotBlank
    @Email
    @Size(max = 320)
    String email,

    @NotBlank
    @Size(min = 2, max = 120)
    String displayName,

    @NotBlank
    @Size(min = 12, max = 72)
    String password,

    @NotBlank
    @Size(min = 2, max = 160)
    String businessName

) {

    @AssertTrue(message = "Password exceeds the supported UTF-8 byte length")
    public boolean isPasswordWithinBcryptLimit() {
        return password != null
            && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
