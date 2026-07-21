package com.joaopablo.ecommerce.product.specification;

import com.joaopablo.ecommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (root, query, builder) ->
                builder.like(
                        builder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> isActive(Boolean active) {
        return (root, query, builder) ->
                builder.equal(root.get("active"), active);
    }

    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, builder) ->
                builder.equal(root.get("category").get("id"), categoryId);
    }

}