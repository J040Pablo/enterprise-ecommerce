package com.joaopablo.ecommerce.category.service;

import com.joaopablo.ecommerce.category.dto.request.CategoryFilterRequest;
import com.joaopablo.ecommerce.category.dto.request.CreateCategoryRequest;
import com.joaopablo.ecommerce.category.dto.request.UpdateCategoryRequest;
import com.joaopablo.ecommerce.category.dto.response.CategoryResponse;
import com.joaopablo.ecommerce.category.entity.Category;
import com.joaopablo.ecommerce.category.exception.CategoryNotFoundException;
import com.joaopablo.ecommerce.category.mapper.CategoryMapper;
import com.joaopablo.ecommerce.category.repository.CategoryRepository;
import com.joaopablo.ecommerce.category.specification.CategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryResponse create(CreateCategoryRequest request) {

        Category category = mapper.toEntity(request);

        Category savedCategory = repository.save(category);

        return mapper.toResponse(savedCategory);
    }

    public Page<CategoryResponse> findAll(CategoryFilterRequest filter, Pageable pageable) {

        Specification<Category> specification = Specification.unrestricted();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            specification = specification.and(
                    CategorySpecification.hasName(filter.getName())
            );
        }

        if (filter.getActive() != null) {
            specification = specification.and(
                    CategorySpecification.isActive(filter.getActive())
            );
        }

        return repository.findAll(specification, pageable)
                .map(mapper::toResponse);
    }

    public CategoryResponse findById(UUID id) {
        return mapper.toResponse(findEntityById(id));
    }

    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {

        Category category = findEntityById(id);

        mapper.updateEntity(request, category);

        Category savedCategory = repository.save(category);

        return mapper.toResponse(savedCategory);
    }

    public void delete(UUID id) {

        Category category = findEntityById(id);

        repository.delete(category);
    }

    private Category findEntityById(UUID id) {

        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}