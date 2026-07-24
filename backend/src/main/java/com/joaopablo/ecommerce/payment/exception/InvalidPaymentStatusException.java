package com.joaopablo.ecommerce.payment.exception;

import com.joaopablo.ecommerce.common.exception.BusinessException;

public class InvalidPaymentStatusException extends BusinessException {

    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
