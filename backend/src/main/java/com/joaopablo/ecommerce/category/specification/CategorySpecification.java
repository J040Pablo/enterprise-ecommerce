package com.joaopablo.ecommerce.category.specification;

import com.joaopablo.ecommerce.category.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

    public static Specification<Category> hasName(String name) {

        return (root, query, builder) ->
                builder.like(
                        builder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );

    }

    public static Specification<Category> isActive(Boolean active) {

        return (root, query, builder) ->
                builder.equal(root.get("active"), active);

    }

}