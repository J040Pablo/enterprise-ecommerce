package com.joaopablo.ecommerce.test.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.joaopablo.ecommerce.auth.repository.RefreshTokenRepository;
import com.joaopablo.ecommerce.cart.repository.CartRepository;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import com.joaopablo.ecommerce.category.repository.CategoryRepository;
import com.joaopablo.ecommerce.auth.repository.UserRoleRepository;
import com.joaopablo.ecommerce.auth.repository.UserRepository;
import com.joaopablo.ecommerce.auth.repository.RoleRepository;
import com.joaopablo.ecommerce.test.util.TestDataSeeder;

/**
 * Centralized test data cleaner that deletes data in dependency order to avoid
 * referential integrity violations during tests.
 */
@Component
public class TestDataCleaner {

    private final RefreshTokenRepository refreshTokenRepository;
    private final CartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TestDataSeeder testDataSeeder;

    @Autowired
    public TestDataCleaner(RefreshTokenRepository refreshTokenRepository,
                           CartRepository cartRepository,
                           InventoryRepository inventoryRepository,
                           ProductRepository productRepository,
                           CategoryRepository categoryRepository,
                           UserRoleRepository userRoleRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           TestDataSeeder testDataSeeder) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.cartRepository = cartRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.testDataSeeder = testDataSeeder;
    }

    @Transactional
    public void cleanAll() {
        // Delete in dependency order to respect FK constraints:
        // refresh_tokens -> cart -> inventory -> product -> category -> user_roles -> users -> roles
        refreshTokenRepository.deleteAll();
        cartRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Re-seed essential test data (roles, etc.) so tests that expect baseline data work
        testDataSeeder.seedDefaults();
    }
}
