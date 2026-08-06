package com.joaopablo.ecommerce.product.repository;

import com.joaopablo.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends
                JpaRepository<Product, UUID>,
                JpaSpecificationExecutor<Product> {

        @EntityGraph(attributePaths = "category")
        List<Product> findByNameContainingIgnoreCase(String name);

        @EntityGraph(attributePaths = "category")
        Page<Product> findByNameContainingIgnoreCase(
                        String name,
                        Pageable pageable);

        @EntityGraph(attributePaths = "category")
        Page<Product> findByActive(
                        Boolean active,
                        Pageable pageable);

        @EntityGraph(attributePaths = "category")
        @Override
        Page<Product> findAll(Pageable pageable);

        @EntityGraph(attributePaths = "category")
        @Override
        Page<Product> findAll(
                        Specification<Product> spec,
                        Pageable pageable);

        @EntityGraph(attributePaths = "category")
        @Override
        Optional<Product> findById(UUID id);

        boolean existsByCategoryId(UUID categoryId);

}