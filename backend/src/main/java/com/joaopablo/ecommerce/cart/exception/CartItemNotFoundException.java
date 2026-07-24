package com.joaopablo.ecommerce.cart.exception;

import com.joaopablo.ecommerce.common.exception.ResourceNotFoundException;
import java.util.UUID;

public class CartItemNotFoundException extends ResourceNotFoundException {

    public CartItemNotFoundException(UUID productId) {
        super("Item with product ID " + productId + " not found in the shopping cart.");
    }
}
