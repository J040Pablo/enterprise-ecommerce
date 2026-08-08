package com.joaopablo.ecommerce.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public JwtService(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs,
            Environment environment
    ) {
        boolean production = isProductionProfile(environment);

        if (secret == null || secret.isBlank()) {
            if (production) {
                throw new IllegalStateException(
                        "JWT_SECRET environment variable must be set in production. "
                                + "Refusing to start with an ephemeral signing key."
                );
            }
            log.warn("JWT secret not set. Generating an ephemeral signing key — not suitable for production. Set JWT_SECRET env var to provide a persistent secret.");
            this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.expirationMs = expirationMs;
    }

    private static boolean isProductionProfile(Environment environment) {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile));
    }

    /**
     * Issues an access token. {@code sub} remains the email (used by JwtAuthenticationFilter);
     * {@code userId} carries the persisted user UUID for clients that decode the JWT.
     */
    public String generateToken(String email, UUID userId) {
        Objects.requireNonNull(userId, "userId is required to issue an access token");
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
