
package com.solocrew.platform.identity;

import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerLoginRequest(

    @NotBlank
    @Email
    @Size(max = 320)
    String email,

    @NotBlank
    @Size(max = 72)
    String password

) {

    @AssertTrue(message = "Password exceeds the supported UTF-8 byte length")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null
            || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
