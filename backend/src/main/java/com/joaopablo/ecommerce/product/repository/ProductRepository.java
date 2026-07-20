package com.joaopablo.ecommerce.product.repository;

import com.joaopablo.ecommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByNameContainingIgnoreCase(String name);
}
