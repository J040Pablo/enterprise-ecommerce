package com.joaopablo.ecommerce.inventory.mapper;

import com.joaopablo.ecommerce.inventory.dto.response.InventoryResponse;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {


    public InventoryResponse toResponse(Inventory inventory) {

        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .quantity(inventory.getQuantity())
                .build();

    }

}