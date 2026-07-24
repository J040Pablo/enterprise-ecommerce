package com.joaopablo.ecommerce.payment.exception;

import com.joaopablo.ecommerce.common.exception.ConflictException;

import java.util.UUID;

public class PaymentAlreadyExistsException extends ConflictException {

    public PaymentAlreadyExistsException(UUID orderId) {
        super("Payment already exists for order id: " + orderId);
    }
}
