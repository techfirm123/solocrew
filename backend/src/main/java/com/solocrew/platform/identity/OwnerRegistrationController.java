package com.solocrew.platform.identity;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solocrew.platform.security.PublicRequestRateLimiter;

@RestController
@RequestMapping("/api/v1/public/registrations")
public class OwnerRegistrationController {

    private static final RegistrationAcceptedResponse ACCEPTED =
        new RegistrationAcceptedResponse(
            "If registration can proceed, a verification email will be sent."
        );

    private final OwnerRegistrationService registrationService;

    private final PublicRequestRateLimiter rateLimiter;

    public OwnerRegistrationController(
        OwnerRegistrationService registrationService,
        PublicRequestRateLimiter rateLimiter
    ) {
        this.registrationService = registrationService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<RegistrationAcceptedResponse> register(
        @Valid @RequestBody OwnerRegistrationRequest request,
        HttpServletRequest servletRequest
    ) {
        boolean allowed = rateLimiter.allow(
            "OWNER_REGISTRATION",
            servletRequest.getRemoteAddr(),
            Duration.ofHours(1),
            10
        );

        if (!allowed) {
            return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ACCEPTED);
        }

        try {
            registrationService.register(request);
        } catch (DuplicateKeyException ignored) {
            // Do not disclose whether an email address already exists.
        }

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ACCEPTED);
    }

    public record RegistrationAcceptedResponse(
        String message
    ) {
    }
}
