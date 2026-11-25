package com.solocrew.platform.identity;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solocrew.platform.security.PublicRequestRateLimiter;

@RestController
@RequestMapping("/api/v1/public/email-verifications")
public class EmailVerificationController {

    private static final MessageResponse VERIFIED =
        new MessageResponse(
            "Email address verified. You can now sign in."
        );

    private static final MessageResponse INVALID =
        new MessageResponse(
            "The verification link is invalid or has expired."
        );

    private static final MessageResponse RESEND_ACCEPTED =
        new MessageResponse(
            "If the account can be verified, a new email will be sent."
        );

    private final EmailVerificationService verificationService;

    private final PublicRequestRateLimiter rateLimiter;

    public EmailVerificationController(
        EmailVerificationService verificationService,
        PublicRequestRateLimiter rateLimiter
    ) {
        this.verificationService = verificationService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> verify(
        @Valid @RequestBody EmailVerificationRequest request
    ) {
        boolean verified = verificationService.verify(
            request.token()
        );

        if (!verified) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(INVALID);
        }

        return ResponseEntity.ok(VERIFIED);
    }

    @PostMapping("/resend")
    public ResponseEntity<MessageResponse> resend(
        @Valid @RequestBody EmailVerificationResendRequest request,
        HttpServletRequest servletRequest
    ) {
        boolean allowed = rateLimiter.allow(
            "EMAIL_VERIFICATION_RESEND",
            servletRequest.getRemoteAddr(),
            Duration.ofMinutes(15),
            5
        );

        if (allowed) {
            verificationService.resend(request.email());
        }

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(RESEND_ACCEPTED);
    }

    public record MessageResponse(
        String message
    ) {
    }
}
