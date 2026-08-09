package com.joaopablo.ecommerce.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSecretValidatorTest {

    @Test
    void prodWithoutSecretShouldFail() {
        assertFailsWith("prod", "");
    }

    @Test
    void productionAliasWithShortSecretShouldFail() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"production"});

        JwtSecretValidator validator = new JwtSecretValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret", "too-short");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                validator::validateJwtSecret
        );
        assertTrue(ex.getMessage().contains("32 characters"));
    }

    @Test
    void dockerWithoutSecretShouldFail() {
        assertFailsWith("docker", "");
    }

    @Test
    void testProfileWithoutSecretShouldFail() {
        assertFailsWith("test", "   ");
    }

    @Test
    void prodWithValidSecretShouldPass() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        JwtSecretValidator validator = new JwtSecretValidator(environment);
        ReflectionTestUtils.setField(
                validator,
                "jwtSecret",
                "secure-production-secret-key-32chars!"
        );

        assertDoesNotThrow(validator::validateJwtSecret);
    }

    @Test
    void devWithoutSecretShouldPass() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        JwtSecretValidator validator = new JwtSecretValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret", "");

        assertDoesNotThrow(validator::validateJwtSecret);
    }

    private static void assertFailsWith(String profile, String secret) {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{profile});

        JwtSecretValidator validator = new JwtSecretValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret", secret);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                validator::validateJwtSecret
        );
        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }
}
