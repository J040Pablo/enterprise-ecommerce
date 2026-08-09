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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Local-only admin convenience. Active solely under {@code SPRING_PROFILE=dev}.
 * <p>
 * Ensures {@code admin@ecommerce.com} exists with the well-known local password
 * so developers can sign in after Flyway V22 revokes that credential globally.
 * Never runs in prod / production / docker.
 */
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataSeeder.class);

    /** Local development only — never used outside {@code @Profile("dev")}. */
    private static final String LOCAL_ADMIN_EMAIL = "admin@ecommerce.com";
    private static final String LOCAL_ADMIN_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("ADMIN")
                            .description("Administrator role")
                            .build()));

            roleRepository.findByName("CUSTOMER")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("CUSTOMER")
                            .description("Customer role")
                            .build()));

            userRepository.findByEmail(LOCAL_ADMIN_EMAIL).ifPresentOrElse(existing -> {
                existing.setPassword(passwordEncoder.encode(LOCAL_ADMIN_PASSWORD));
                existing.setEnabled(true);
                existing.setEmailVerified(true);
                ensureAdminRole(existing, adminRole);
                userRepository.save(existing);
                log.info("Ensured local development admin user is enabled");
            }, () -> {
                User adminUser = User.builder()
                        .email(LOCAL_ADMIN_EMAIL)
                        .password(passwordEncoder.encode(LOCAL_ADMIN_PASSWORD))
                        .firstName("Admin")
                        .lastName("System")
                        .provider(AuthProvider.LOCAL)
                        .enabled(true)
                        .emailVerified(true)
                        .build();

                UserRole userRole = UserRole.builder()
                        .user(adminUser)
                        .role(adminRole)
                        .build();
                adminUser.getUserRoles().add(userRole);
                userRepository.save(adminUser);
                log.info("Created local development admin user");
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
