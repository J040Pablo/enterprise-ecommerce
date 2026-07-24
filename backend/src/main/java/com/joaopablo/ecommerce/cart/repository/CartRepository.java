package com.joaopablo.ecommerce.cart.repository;

import com.joaopablo.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Cart> findByUserEmail(String email);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Cart> findByUserId(Long userId);
}
