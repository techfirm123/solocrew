
package com.solocrew.platform.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/owner")
public class OwnerSessionController {

    private final OwnerWorkspaceAccessService workspaceAccessService;
    private final OwnerAuthenticationService authenticationService;

    public OwnerSessionController(
        OwnerWorkspaceAccessService workspaceAccessService,
        OwnerAuthenticationService authenticationService
    ) {
        this.workspaceAccessService = workspaceAccessService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/me")
    public OwnerSessionResponse currentOwner(
        @AuthenticationPrincipal OwnerUserPrincipal principal
    ) {
        return workspaceAccessService
            .findActiveOwnerContext(principal.userId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No active business workspace is available."
            ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @AuthenticationPrincipal OwnerUserPrincipal principal,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        authenticationService.logout(
            principal,
            servletRequest,
            servletResponse
        );

        return ResponseEntity.noContent().build();
    }
}
