package com.joaopablo.ecommerce.shipping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CreateShippingRequest", description = "Payload for creating a shipment for a confirmed and paid order")
public class CreateShippingRequest {

    @NotNull(message = "Order ID is required")
    @Schema(
            description = "UUID of the confirmed order to be shipped",
            example = "c3d4e5f6-0000-4bcd-9abc-222222222222",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID orderId;

    @NotBlank(message = "Carrier is required")
    @Schema(
            description = "Carrier company responsible for the delivery (e.g., Correios, Fedex, DHL)",
            example = "Correios",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String carrier;

    @NotNull(message = "Estimated delivery is required")
    @Schema(
            description = "Estimated delivery date in ISO-8601 format (yyyy-MM-dd)",
            example = "2026-08-10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate estimatedDelivery;
}

