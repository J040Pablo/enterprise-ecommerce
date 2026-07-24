package com.joaopablo.ecommerce.order.exception;

import com.joaopablo.ecommerce.common.exception.BusinessException;

public class InvalidOrderStatusException extends BusinessException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
