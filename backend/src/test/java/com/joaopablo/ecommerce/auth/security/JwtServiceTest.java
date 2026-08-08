package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private static final String SECRET =
            "4M7pQ2kL9xV1nB8sT6wY3eR5uI0oP4aD7fG2hJ9kL1mN8qR6";

    private static final long EXPIRATION_MS = 86400000L;

    private static final UUID USER_ID =
            UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    private Environment environment;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        jwtService = new JwtService(SECRET, EXPIRATION_MS, environment);
    }

    @Test
    void generateTokenShouldProduceValidJwtForEmailAndUserId() {
        String token = jwtService.generateToken("user@email.com", USER_ID);

        assertNotNull(token);
        assertEquals("user@email.com", jwtService.extractUsername(token));

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals(USER_ID.toString(), claims.get("userId", String.class));
        assertEquals(EXPIRATION_MS, jwtService.getExpirationMs());
    }

    @Test
    void isTokenValidShouldReturnTrueForMatchingUser() {
        String token = jwtService.generateToken("user@email.com", USER_ID);

        UserDetails userDetails =
                User.withUsername("user@email.com")
                        .password("secret")
                        .roles("CUSTOMER")
                        .build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken("user@email.com", USER_ID);

        UserDetails userDetails =
                User.withUsername("other@email.com")
                        .password("secret")
                        .roles("CUSTOMER")
                        .build();

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void blankSecretInNonProductionShouldCreateEphemeralKey() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        JwtService ephemeralService = new JwtService("", EXPIRATION_MS, environment);
        String token = ephemeralService.generateToken("user@email.com", USER_ID);

        assertNotNull(token);
        assertEquals("user@email.com", ephemeralService.extractUsername(token));
    }

    @Test
    void blankSecretInProductionShouldFailFast() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new JwtService("", EXPIRATION_MS, environment)
        );

        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void blankSecretInProductionAliasShouldFailFast() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"production"});

        assertThrows(
                IllegalStateException.class,
                () -> new JwtService("   ", EXPIRATION_MS, environment)
        );
    }
}
