package org.kumaran.security;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiRequestAuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestAuditLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(UUID.randomUUID().toString());
        long startTime = System.nanoTime();
        response.setHeader("X-Request-Id", requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000L;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? String.valueOf(authentication.getPrincipal()) : "anonymous";
            String role = authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()
                    ? authentication.getAuthorities().iterator().next().getAuthority()
                    : "none";
            boolean hasBearer = Optional.ofNullable(request.getHeader("Authorization"))
                    .map(value -> value.startsWith("Bearer "))
                    .orElse(false);
            String authFailure = Optional.ofNullable((String) request.getAttribute("auth.failure")).orElse("none");

            if (response.getStatus() >= 400) {
                logger.warn(
                        "api_request requestId={} method={} path={} status={} durationMs={} username={} role={} hasBearer={} authFailure={}",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        durationMs,
                        username,
                        role,
                        hasBearer,
                        authFailure);
            } else {
                logger.info(
                        "api_request requestId={} method={} path={} status={} durationMs={} username={} role={} hasBearer={}",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        durationMs,
                        username,
                        role,
                        hasBearer);
            }
        }
    }
}