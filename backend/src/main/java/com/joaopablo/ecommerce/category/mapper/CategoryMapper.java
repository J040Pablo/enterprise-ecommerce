package com.joaopablo.ecommerce.category.mapper;

import com.joaopablo.ecommerce.category.dto.request.CreateCategoryRequest;
import com.joaopablo.ecommerce.category.dto.request.UpdateCategoryRequest;
import com.joaopablo.ecommerce.category.dto.response.CategoryResponse;
import com.joaopablo.ecommerce.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {

        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

    }

    public CategoryResponse toResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .build();

    }

    public void updateEntity(UpdateCategoryRequest request, Category category) {

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

    }
}