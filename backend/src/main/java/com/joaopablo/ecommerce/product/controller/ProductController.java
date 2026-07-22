package com.joaopablo.ecommerce.product.controller;

import com.joaopablo.ecommerce.product.dto.request.CreateProductRequest;
import com.joaopablo.ecommerce.product.dto.request.ProductFilterRequest;
import com.joaopablo.ecommerce.product.dto.request.UpdateProductRequest;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            ProductFilterRequest filter,
            Pageable pageable) {

        return ResponseEntity.ok(
                service.findAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}