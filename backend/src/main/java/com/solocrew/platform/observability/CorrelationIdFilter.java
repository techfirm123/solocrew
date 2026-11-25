package com.solocrew.platform.observability;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";

    public static final String MDC_KEY = "correlationId";

    public static final String REQUEST_ATTRIBUTE =
        CorrelationIdFilter.class.getName() + ".correlationId";

    private static final Pattern VALID_CORRELATION_ID =
        Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}");

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String previousCorrelationId = MDC.get(MDC_KEY);

        String correlationId = resolveCorrelationId(
            request.getHeader(HEADER_NAME)
        );

        request.setAttribute(
            REQUEST_ATTRIBUTE,
            correlationId
        );

        response.setHeader(
            HEADER_NAME,
            correlationId
        );

        MDC.put(MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (previousCorrelationId == null) {
                MDC.remove(MDC_KEY);
            } else {
                MDC.put(MDC_KEY, previousCorrelationId);
            }
        }
    }

    private String resolveCorrelationId(String suppliedValue) {
        if (
            suppliedValue != null
                && VALID_CORRELATION_ID.matcher(suppliedValue).matches()
        ) {
            return suppliedValue;
        }

        return UUID.randomUUID().toString();
    }
}
