package com.joaopablo.ecommerce.cart.mapper;

import com.joaopablo.ecommerce.cart.dto.response.CartItemResponse;
import com.joaopablo.ecommerce.cart.dto.response.CartResponse;
import com.joaopablo.ecommerce.cart.entity.Cart;
import com.joaopablo.ecommerce.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "total", source = "total")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "subtotal", source = "subtotal")
    CartItemResponse toItemResponse(CartItem item);
}
