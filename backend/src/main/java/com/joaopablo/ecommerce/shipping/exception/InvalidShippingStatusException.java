package com.joaopablo.ecommerce.shipping.exception;

import com.joaopablo.ecommerce.common.exception.BusinessException;

public class InvalidShippingStatusException extends BusinessException {

    public InvalidShippingStatusException(String message) {
        super(message);
    }
}
