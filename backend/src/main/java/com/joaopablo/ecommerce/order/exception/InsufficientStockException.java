package com.joaopablo.ecommerce.order.exception;

import com.joaopablo.ecommerce.common.exception.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
