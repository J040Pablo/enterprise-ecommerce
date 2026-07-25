package com.joaopablo.ecommerce.shipping.exception;

import com.joaopablo.ecommerce.common.exception.ConflictException;

import java.util.UUID;

public class ShippingAlreadyExistsException extends ConflictException {

    public ShippingAlreadyExistsException(UUID orderId) {
        super("Shipping already exists for order id: " + orderId);
    }
}
