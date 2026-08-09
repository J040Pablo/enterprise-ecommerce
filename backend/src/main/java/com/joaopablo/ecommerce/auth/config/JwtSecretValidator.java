package com.joaopablo.ecommerce.auth.config;

import com.joaopablo.ecommerce.common.util.ActiveProfiles;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class JwtSecretValidator {

    private final Environment environment;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @PostConstruct
    public void validateJwtSecret() {
        if (!ActiveProfiles.requiresStrictSecrets(environment)) {
            return;
        }

        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable must be set outside the dev profile. "
                            + "Please provide a secure JWT secret before starting the application."
            );
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters long. "
                            + "Current length: " + jwtSecret.length()
            );
        }
    }
}
