package com.joaopablo.ecommerce.product.exception;

import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(UUID id) {
        super("Produto com id " + id + " não encontrado.");
    }

}