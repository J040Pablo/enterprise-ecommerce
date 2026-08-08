package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "4M7pQ2kL9xV1nB8sT6wY3eR5uI0oP4aD7fG2hJ9kL1mN8qR6";

    private static final long EXPIRATION_MS = 86400000L;

    private static final UUID USER_ID =
            UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");


    private JwtService jwtService;


    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                SECRET,
                EXPIRATION_MS
        );
    }


    @Test
    void generateTokenShouldProduceValidJwtForEmailAndUserId() {

        String token = jwtService.generateToken("user@email.com", USER_ID);

        assertNotNull(token);
        assertEquals(
                "user@email.com",
                jwtService.extractUsername(token)
        );

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals(USER_ID.toString(), claims.get("userId", String.class));

        assertEquals(
                EXPIRATION_MS,
                jwtService.getExpirationMs()
        );
    }


    @Test
    void isTokenValidShouldReturnTrueForMatchingUser() {

        String token = jwtService.generateToken("user@email.com", USER_ID);

        UserDetails userDetails =
                User.withUsername("user@email.com")
                        .password("secret")
                        .roles("CUSTOMER")
                        .build();


        assertTrue(
                jwtService.isTokenValid(token, userDetails)
        );
    }


    @Test
    void isTokenValidShouldReturnFalseForDifferentUser() {

        String token = jwtService.generateToken("user@email.com", USER_ID);

        UserDetails userDetails =
                User.withUsername("other@email.com")
                        .password("secret")
                        .roles("CUSTOMER")
                        .build();


        assertFalse(
                jwtService.isTokenValid(token, userDetails)
        );
    }
}