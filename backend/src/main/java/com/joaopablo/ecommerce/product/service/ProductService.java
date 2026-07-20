package com.joaopablo.ecommerce.product.service;

import com.joaopablo.ecommerce.product.dto.request.CreateProductRequest;
import com.joaopablo.ecommerce.product.dto.request.UpdateProductRequest;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.exception.ProductNotFoundException;
import com.joaopablo.ecommerce.product.mapper.ProductMapper;
import com.joaopablo.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductResponse create(CreateProductRequest request) {
        Product product = mapper.toEntity(request);

        Product savedProduct = repository.save(product);

        return mapper.toResponse(savedProduct);
    }

    public Page<ProductResponse> findAll(Pageable pageable) {

        return repository.findAll(pageable)
                .map(mapper::toResponse);

    }

    public ProductResponse findById(UUID id) {
        return mapper.toResponse(findEntityById(id));
    }

    private Product findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductResponse update(UUID id, UpdateProductRequest request) {

        Product product = findEntityById(id);

        mapper.updateEntity(request, product);

        Product savedProduct = repository.save(product);

        return mapper.toResponse(savedProduct);
    }

    public void delete(UUID id) {
        Product product = findEntityById(id);
        repository.delete(product);
    }
}