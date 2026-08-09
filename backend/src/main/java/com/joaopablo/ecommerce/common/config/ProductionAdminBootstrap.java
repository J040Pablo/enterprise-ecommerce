package com.joaopablo.ecommerce.common.config;

import com.joaopablo.ecommerce.auth.entity.AuthProvider;
import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.entity.UserRole;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provisions an admin from environment variables outside {@code dev}/{@code test}.
 * <p>
 * Set {@code ADMIN_EMAIL} and {@code ADMIN_PASSWORD} (min 12 chars).
 * The password is never logged. If unset, startup continues but no admin is created —
 * operators must provision one intentionally.
 */
@Configuration
@Profile("!dev & !test")
@RequiredArgsConstructor
public class ProductionAdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ProductionAdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    public CommandLineRunner provisionAdminFromEnvironment() {
        return args -> {
            if (adminEmail == null || adminEmail.isBlank()
                    || adminPassword == null || adminPassword.isBlank()) {
                log.warn(
                        "ADMIN_EMAIL/ADMIN_PASSWORD not set — skipping admin bootstrap. "
                                + "Ensure an administrator account exists before exposing the API."
                );
                return;
            }

            if (adminPassword.length() < MIN_PASSWORD_LENGTH) {
                throw new IllegalStateException(
                        "ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters."
                );
            }

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("ADMIN")
                            .description("System administrator")
                            .build()));

            String email = adminEmail.trim().toLowerCase();
            String encoded = passwordEncoder.encode(adminPassword);

            userRepository.findByEmail(email).ifPresentOrElse(existing -> {
                existing.setPassword(encoded);
                existing.setEnabled(true);
                existing.setEmailVerified(true);
                ensureAdminRole(existing, adminRole);
                userRepository.save(existing);
                log.info("Updated administrator account from environment configuration");
            }, () -> {
                User admin = User.builder()
                        .email(email)
                        .password(encoded)
                        .firstName("Admin")
                        .lastName("System")
                        .provider(AuthProvider.LOCAL)
                        .enabled(true)
                        .emailVerified(true)
                        .build();
                admin.getUserRoles().add(UserRole.builder()
                        .user(admin)
                        .role(adminRole)
                        .build());
                userRepository.save(admin);
                log.info("Created administrator account from environment configuration");
            });
        };
    }

    private void ensureAdminRole(User user, Role adminRole) {
        boolean hasAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> "ADMIN".equals(ur.getRole().getName()));
        if (!hasAdmin) {
            user.getUserRoles().add(UserRole.builder()
                    .user(user)
                    .role(adminRole)
                    .build());
        }
    }
}
