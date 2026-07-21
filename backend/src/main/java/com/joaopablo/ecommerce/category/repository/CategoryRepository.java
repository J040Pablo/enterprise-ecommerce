package com.joaopablo.ecommerce.category.repository;

import com.joaopablo.ecommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends
        JpaRepository<Category, UUID>,
        JpaSpecificationExecutor<Category> {
}