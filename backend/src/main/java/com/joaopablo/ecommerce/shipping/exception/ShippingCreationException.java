package com.joaopablo.ecommerce.shipping.exception;

import com.joaopablo.ecommerce.common.exception.BusinessException;

public class ShippingCreationException extends BusinessException {

    public ShippingCreationException(String message) {
        super(message);
    }
}
