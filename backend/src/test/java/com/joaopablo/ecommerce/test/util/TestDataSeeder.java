package com.joaopablo.ecommerce.test.util;

import com.joaopablo.ecommerce.auth.entity.Role;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Seeds minimal, essential test data (like roles) required by integration tests.
 * Kept in test sources only so it never runs in production.
 */
@Component
public class TestDataSeeder {

    private final RoleRepository roleRepository;

    @Autowired
    public TestDataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void seedDefaults() {
        if (!roleRepository.existsByName("CUSTOMER")) {
            roleRepository.save(Role.builder().name("CUSTOMER").description("Customer role").build());
        }

        // Add other essential roles or seed data here if needed in future
    }
}
