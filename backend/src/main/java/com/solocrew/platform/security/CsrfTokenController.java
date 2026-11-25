package com.solocrew.platform.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/csrf")
public class CsrfTokenController {

    @GetMapping
    public CsrfTokenResponse getToken(CsrfToken csrfToken) {
        return new CsrfTokenResponse(
            csrfToken.getHeaderName(),
            csrfToken.getParameterName(),
            csrfToken.getToken()
        );
    }

    public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
    ) {
    }
}
