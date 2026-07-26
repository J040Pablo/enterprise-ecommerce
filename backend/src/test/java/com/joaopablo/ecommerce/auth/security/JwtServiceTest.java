package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "4M7pQ2kL9xV1nB8sT6wY3eR5uI0oP4aD7fG2hJ9kL1mN8qR6";

    private static final long EXPIRATION_MS = 86400000L;


    private JwtService jwtService;


    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                SECRET,
                EXPIRATION_MS
        );
    }


    @Test
    void generateTokenShouldProduceValidJwtForEmail() {

        String token = jwtService.generateToken("user@email.com");

        assertNotNull(token);
        assertEquals(
                "user@email.com",
                jwtService.extractUsername(token)
        );

        assertEquals(
                EXPIRATION_MS,
                jwtService.getExpirationMs()
        );
    }


    @Test
    void isTokenValidShouldReturnTrueForMatchingUser() {

        String token = jwtService.generateToken("user@email.com");

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

        String token = jwtService.generateToken("user@email.com");

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