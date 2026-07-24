package com.joaopablo.ecommerce.payment.exception;

import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(UUID id) {
        super("Payment not found with id: " + id);
    }
}
