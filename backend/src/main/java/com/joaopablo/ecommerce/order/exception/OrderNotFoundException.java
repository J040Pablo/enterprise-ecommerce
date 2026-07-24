package com.joaopablo.ecommerce.order.exception;

import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(UUID id) {
        super("Order not found with id: " + id);
    }
}
