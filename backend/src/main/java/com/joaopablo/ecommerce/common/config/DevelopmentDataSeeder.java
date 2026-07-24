package com.joaopablo.ecommerce.common.config;

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

import java.util.Optional;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("ADMIN")
                                .description("Administrator role")
                                .build();

                        return roleRepository.save(role);
                    });

            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("CUSTOMER")
                                .description("Customer role")
                                .build();

                        return roleRepository.save(role);
                    });

            String adminEmail = "admin@ecommerce.com";

            if (!userRepository.existsByEmail(adminEmail)) {

                User adminUser = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode("Admin@123"))
                        .firstName("Admin")
                        .lastName("System")
                        .enabled(true)
                        .emailVerified(true)
                        .build();

                UserRole userRole = UserRole.builder()
                        .user(adminUser)
                        .role(adminRole)
                        .build();

                adminUser.getUserRoles().add(userRole);

                userRepository.save(adminUser);

                log.info("Created admin user");
            }
        };
    }
}
