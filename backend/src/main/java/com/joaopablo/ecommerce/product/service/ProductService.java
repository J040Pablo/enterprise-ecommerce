package com.joaopablo.ecommerce.product.service;

import com.joaopablo.ecommerce.category.entity.Category;
import com.joaopablo.ecommerce.category.exception.CategoryNotFoundException;
import com.joaopablo.ecommerce.category.repository.CategoryRepository;
import com.joaopablo.ecommerce.product.dto.request.CreateProductRequest;
import com.joaopablo.ecommerce.product.dto.request.ProductFilterRequest;
import com.joaopablo.ecommerce.product.dto.request.UpdateProductRequest;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.exception.ProductNotFoundException;
import com.joaopablo.ecommerce.product.mapper.ProductMapper;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import com.joaopablo.ecommerce.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final CategoryRepository categoryRepository;

    public ProductResponse create(CreateProductRequest request) {

        Product product = mapper.toEntity(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));

        product.setCategory(category);

        Product savedProduct = repository.save(product);

        return mapper.toResponse(savedProduct);

    }

    public Page<ProductResponse> findAll(ProductFilterRequest filter, Pageable pageable) {

        Specification<Product> specification = Specification.where(null);

        if (filter.getName() != null && !filter.getName().isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasName(filter.getName())
            );
        }

        if (filter.getMinPrice() != null) {
            specification = specification.and(
                    ProductSpecification.minPrice(filter.getMinPrice())
            );
        }

        if (filter.getMaxPrice() != null) {
            specification = specification.and(
                    ProductSpecification.maxPrice(filter.getMaxPrice())
            );
        }

        if (filter.getActive() != null) {
            specification = specification.and(
                    ProductSpecification.isActive(filter.getActive())
            );
        }

        if (filter.getCategoryId() != null) {
            specification = specification.and(
                    ProductSpecification.hasCategory(filter.getCategoryId())
            );
        }

        return repository.findAll(specification, pageable)
                .map(mapper::toResponse);

    }

    public ProductResponse findById(UUID id) {
        return mapper.toResponse(findEntityById(id));
    }

    public ProductResponse update(UUID id, UpdateProductRequest request) {

        Product product = findEntityById(id);

        mapper.updateEntity(request, product);

        if (request.getCategoryId() != null) {

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));

            product.setCategory(category);

        }

        Product savedProduct = repository.save(product);

        return mapper.toResponse(savedProduct);

    }

    public void delete(UUID id) {

        Product product = findEntityById(id);

        repository.delete(product);

    }

    private Product findEntityById(UUID id) {

        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

    }

}