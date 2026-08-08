package com.joaopablo.ecommerce.auth.config;

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
        String[] activeProfiles = environment.getActiveProfiles();
        
        // In production profile, JWT secret must be set
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                    throw new IllegalStateException(
                        "JWT_SECRET environment variable must be set in production. " +
                        "Please provide a secure JWT secret before deploying to production."
                    );
                }
                // Minimum length validation for security
                if (jwtSecret.length() < 32) {
                    throw new IllegalStateException(
                        "JWT_SECRET must be at least 32 characters long for production security. " +
                        "Current length: " + jwtSecret.length()
                    );
                }
                break;
            }
        }
    }
}
