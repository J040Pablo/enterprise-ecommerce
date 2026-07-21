package com.joaopablo.ecommerce.category.exception;

import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(UUID id) {
        super("Category with id " + id + " not found.");
    }

}