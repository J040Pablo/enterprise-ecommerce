package com.joaopablo.ecommerce.common.util;

import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Helpers for Spring profile checks used by security and bootstrap guards.
 */
public final class ActiveProfiles {

    private ActiveProfiles() {
    }

    public static boolean isDev(Environment environment) {
        return has(environment, "dev");
    }

    /**
     * Any non-{@code dev} profile (including {@code test}, {@code docker}, {@code prod})
     * must use strict secrets — no ephemeral JWT keys.
     */
    public static boolean requiresStrictSecrets(Environment environment) {
        return !isDev(environment);
    }

    private static boolean has(Environment environment, String profile) {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(active -> profile.equalsIgnoreCase(active));
    }
}
