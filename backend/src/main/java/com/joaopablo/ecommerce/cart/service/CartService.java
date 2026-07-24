package com.joaopablo.ecommerce.cart.service;

import com.joaopablo.ecommerce.cart.dto.request.CreateCartRequest;
import com.joaopablo.ecommerce.cart.dto.request.UpdateCartRequest;
import com.joaopablo.ecommerce.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart();

    CartResponse addItemToCart(CreateCartRequest request);

    CartResponse updateItemQuantity(UUID productId, UpdateCartRequest request);

    CartResponse removeItemFromCart(UUID productId);

    void clearCart();
}
