package com.joaopablo.ecommerce.shipping.exception;

import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ShippingNotFoundException extends ResourceNotFoundException {

    public ShippingNotFoundException(UUID id) {
        super("Shipping not found with id: " + id);
    }

    public ShippingNotFoundException(String message) {
        super(message);
    }
}
