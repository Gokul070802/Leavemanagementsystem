package org.kumaran.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.kumaran.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final String secret;
    private final long expirationMs;
    private Key signingKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secret = jwtProperties.getSecret();
        this.expirationMs = jwtProperties.getExpirationMs();
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException("JWT secret must be configured and at least 32 characters long");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getValidationFailureReason(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return null;
        } catch (ExpiredJwtException ex) {
            return "expired";
        } catch (MalformedJwtException ex) {
            return "malformed";
        } catch (SecurityException ex) {
            return "signature_invalid";
        } catch (IllegalArgumentException ex) {
            return "missing_or_empty";
        } catch (Exception ex) {
            logger.warn("Unexpected JWT validation failure", ex);
            return "unknown_validation_error";
        }
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("role", role)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | MalformedJwtException | SecurityException | IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        return parseClaims(token) != null;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }
}

