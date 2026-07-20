package com.joaopablo.ecommerce.product.controller;

import com.joaopablo.ecommerce.product.dto.request.CreateProductRequest;
import com.joaopablo.ecommerce.product.dto.request.UpdateProductRequest;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(Pageable pageable) {

        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
