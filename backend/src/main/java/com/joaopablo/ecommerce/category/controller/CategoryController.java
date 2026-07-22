package com.joaopablo.ecommerce.category.controller;

import com.joaopablo.ecommerce.category.dto.request.CategoryFilterRequest;
import com.joaopablo.ecommerce.category.dto.request.CreateCategoryRequest;
import com.joaopablo.ecommerce.category.dto.request.UpdateCategoryRequest;
import com.joaopablo.ecommerce.category.dto.response.CategoryResponse;
import com.joaopablo.ecommerce.category.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> findAll(
            CategoryFilterRequest filter,
            Pageable pageable) {

        return ResponseEntity.ok(
                service.findAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.findById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

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