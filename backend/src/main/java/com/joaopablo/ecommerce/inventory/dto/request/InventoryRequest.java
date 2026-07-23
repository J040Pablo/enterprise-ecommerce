package com.joaopablo.ecommerce.inventory.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryRequest(

        @NotNull
        UUID productId,

        @NotNull
        Integer quantity

) {}