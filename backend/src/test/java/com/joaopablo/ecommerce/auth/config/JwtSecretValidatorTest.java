package com.joaopablo.ecommerce.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSecretValidatorTest {

    @Test
    void productionWithoutSecretShouldFail() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        JwtSecretValidator validator = new JwtSecretValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret", "");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                validator::validateJwtSecret
        );
        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void productionWithShortSecretShouldFail() {
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
    void productionWithValidSecretShouldPass() {
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
    void nonProductionWithoutSecretShouldPass() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        JwtSecretValidator validator = new JwtSecretValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret", "");

        assertDoesNotThrow(validator::validateJwtSecret);
    }
}
