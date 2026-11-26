
package com.solocrew.platform.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/auth")
public class OwnerAuthenticationController {

    private final OwnerAuthenticationService authenticationService;

    public OwnerAuthenticationController(
        OwnerAuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationMessageResponse> login(
        @Valid @RequestBody OwnerLoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        OwnerAuthenticationOutcome outcome =
            authenticationService.login(
                request,
                servletRequest,
                servletResponse
            );

        return switch (outcome) {
            case AUTHENTICATED ->
                ResponseEntity.ok(
                    new AuthenticationMessageResponse(
                        "Signed in successfully."
                    )
                );

            case INVALID_CREDENTIALS ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                        new AuthenticationMessageResponse(
                            "Email or password is incorrect."
                        )
                    );

            case RATE_LIMITED ->
                ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(
                        new AuthenticationMessageResponse(
                            "Too many sign-in attempts. Please try again later."
                        )
                    );

            case NO_ACTIVE_WORKSPACE ->
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                        new AuthenticationMessageResponse(
                            "This account does not currently have access to an active business workspace."
                        )
                    );
        };
    }

    public record AuthenticationMessageResponse(
        String message
    ) {
    }
}
