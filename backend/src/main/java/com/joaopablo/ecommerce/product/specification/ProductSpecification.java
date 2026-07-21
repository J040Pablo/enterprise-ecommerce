package com.joaopablo.ecommerce.product.specification;

import com.joaopablo.ecommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {


    // Filtro por nome
    public static Specification<Product> hasName(String name) {

        return (root, query, builder) ->

                builder.like(
                        builder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );

    }

    // Filtro por preço mínimo
    public static Specification<Product> minPrice(BigDecimal price){

        return (root, query, builder) ->

                builder.greaterThanOrEqualTo(
                        root.get("price"),
                        price
                );

    }

    // Filtro por preço máximo
    public static Specification<Product> maxPrice(BigDecimal price){

        return (root, query, builder) ->

                builder.lessThanOrEqualTo(
                        root.get("price"),
                        price
                );

    }

    // Produto ativo
    public static Specification<Product> isActive(Boolean active){

        return (root, query, builder) ->

                builder.equal(root.get("active"), active);

    }
}
