
package com.solocrew.platform.identity;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solocrew.platform.audit.AuditActorType;
import com.solocrew.platform.audit.AuditEvent;
import com.solocrew.platform.audit.AuditEventWriter;
import com.solocrew.platform.audit.AuditOutcome;
import com.solocrew.platform.security.PublicRequestRateLimiter;

@Service
public class OwnerAuthenticationService {

    private static final Duration LOGIN_WINDOW =
        Duration.ofMinutes(15);

    private static final int MAXIMUM_IP_ATTEMPTS = 30;

    private static final int MAXIMUM_ACCOUNT_ATTEMPTS = 10;

    private static final String INVALID_CREDENTIALS_METADATA =
        "{\"reason\":\"INVALID_CREDENTIALS\"}";

    private static final String RATE_LIMITED_METADATA =
        "{\"reason\":\"RATE_LIMITED\"}";

    private static final String NO_WORKSPACE_METADATA =
        "{\"reason\":\"NO_ACTIVE_WORKSPACE\"}";

    private final AuthenticationManager authenticationManager;
    private final PublicRequestRateLimiter rateLimiter;
    private final OwnerWorkspaceAccessService workspaceAccessService;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextLogoutHandler logoutHandler;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AuditEventWriter auditEventWriter;
    private final Clock clock;

    public OwnerAuthenticationService(
        AuthenticationManager authenticationManager,
        PublicRequestRateLimiter rateLimiter,
        OwnerWorkspaceAccessService workspaceAccessService,
        SessionAuthenticationStrategy sessionAuthenticationStrategy,
        SecurityContextRepository securityContextRepository,
        SecurityContextLogoutHandler logoutHandler,
        CsrfTokenRepository csrfTokenRepository,
        AuditEventWriter auditEventWriter,
        Clock clock
    ) {
        this.authenticationManager = authenticationManager;
        this.rateLimiter = rateLimiter;
        this.workspaceAccessService = workspaceAccessService;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
        this.logoutHandler = logoutHandler;
        this.csrfTokenRepository = csrfTokenRepository;
        this.auditEventWriter = auditEventWriter;
        this.clock = clock;
    }

    @Transactional
    public OwnerAuthenticationOutcome login(
        OwnerLoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        String normalizedEmail = normalizeEmail(request.email());

        boolean ipAllowed = rateLimiter.allow(
            "OWNER_LOGIN_IP",
            servletRequest.getRemoteAddr(),
            LOGIN_WINDOW,
            MAXIMUM_IP_ATTEMPTS
        );

        boolean accountAllowed = ipAllowed
            && rateLimiter.allow(
            "OWNER_LOGIN_ACCOUNT",
            normalizedEmail,
            LOGIN_WINDOW,
            MAXIMUM_ACCOUNT_ATTEMPTS
        );

        if (!accountAllowed) {
            appendSystemLoginEvent(
                AuditOutcome.DENIED,
                RATE_LIMITED_METADATA
            );

            return OwnerAuthenticationOutcome.RATE_LIMITED;
        }

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    normalizedEmail,
                    request.password()
                )
            );
        } catch (InternalAuthenticationServiceException exception) {
            // Do not disguise database or identity-service failures
            // as ordinary invalid credentials.
            throw exception;
        } catch (AuthenticationException exception) {
            appendSystemLoginEvent(
                AuditOutcome.FAILURE,
                INVALID_CREDENTIALS_METADATA
            );

            return OwnerAuthenticationOutcome.INVALID_CREDENTIALS;
        }

        if (!(authentication.getPrincipal()
            instanceof OwnerUserPrincipal principal)) {
            throw new IllegalStateException(
                "Unexpected principal returned by owner authentication"
            );
        }

        Optional<OwnerSessionResponse> ownerContext =
            workspaceAccessService.findActiveOwnerContext(
                principal.userId()
            );

        if (ownerContext.isEmpty()) {
            appendSystemLoginEvent(
                AuditOutcome.DENIED,
                NO_WORKSPACE_METADATA
            );

            return OwnerAuthenticationOutcome.NO_ACTIVE_WORKSPACE;
        }

        // Create a session before applying the session strategy.
        // This also supports clients that did not previously fetch
        // a CSRF token, while the login POST itself remains CSRF-protected.
        servletRequest.getSession(true);

        // Rotate the session ID and remove the previous CSRF token.
        sessionAuthenticationStrategy.onAuthentication(
            authentication,
            servletRequest,
            servletResponse
        );

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Explicitly persist authentication in Spring Session JDBC.
        securityContextRepository.saveContext(
            context,
            servletRequest,
            servletResponse
        );

        appendUserLoginEvent(
            principal,
            ownerContext.get().workspace().id()
        );

        return OwnerAuthenticationOutcome.AUTHENTICATED;
    }

    @Transactional
    public void logout(
        OwnerUserPrincipal principal,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        Optional<OwnerSessionResponse> ownerContext =
            workspaceAccessService.findActiveOwnerContext(
                principal.userId()
            );

        UUID workspaceId = ownerContext
            .map(context -> context.workspace().id())
            .orElse(null);

        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                Instant.now(clock),
                workspaceId,
                AuditActorType.USER,
                principal.userId(),
                "OWNER_LOGOUT",
                "INVALIDATE",
                "SESSION",
                null,
                AuditOutcome.SUCCESS,
                null,
                "{}"
            )
        );

        // Remove the CSRF token before invalidating the session.
        csrfTokenRepository.saveToken(
            null,
            servletRequest,
            servletResponse
        );

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        // Invalidates the server-side session and clears the
        // SecurityContextHolder and SecurityContextRepository.
        logoutHandler.logout(
            servletRequest,
            servletResponse,
            authentication
        );
    }

    private void appendUserLoginEvent(
        OwnerUserPrincipal principal,
        UUID workspaceId
    ) {
        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                Instant.now(clock),
                workspaceId,
                AuditActorType.USER,
                principal.userId(),
                "OWNER_LOGIN",
                "AUTHENTICATE",
                "SESSION",
                null,
                AuditOutcome.SUCCESS,
                null,
                "{}"
            )
        );
    }

    private void appendSystemLoginEvent(
        AuditOutcome outcome,
        String metadata
    ) {
        auditEventWriter.append(
            new AuditEvent(
                UUID.randomUUID(),
                Instant.now(clock),
                null,
                AuditActorType.SYSTEM,
                null,
                "OWNER_LOGIN",
                "AUTHENTICATE",
                "AUTHENTICATION",
                null,
                outcome,
                null,
                metadata
            )
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
