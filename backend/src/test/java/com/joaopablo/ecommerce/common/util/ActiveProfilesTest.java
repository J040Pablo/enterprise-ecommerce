package com.joaopablo.ecommerce.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActiveProfilesTest {

    @Test
    void isDevOnlyWhenDevProfileActive() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev"});
        assertTrue(ActiveProfiles.isDev(env));
        assertFalse(ActiveProfiles.requiresStrictSecrets(env));
    }

    @Test
    void nonDevProfilesRequireStrictSecrets() {
        for (String profile : new String[]{"test", "docker", "prod", "production"}) {
            Environment env = mock(Environment.class);
            when(env.getActiveProfiles()).thenReturn(new String[]{profile});
            assertFalse(ActiveProfiles.isDev(env), profile);
            assertTrue(ActiveProfiles.requiresStrictSecrets(env), profile);
        }
    }
}
