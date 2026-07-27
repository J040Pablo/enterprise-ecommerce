package com.joaopablo.ecommerce.shipping.dto.response;

import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ShippingResponse", description = "Full shipping record returned by the Shipping API")
public class ShippingResponse {

    @Schema(description = "Unique identifier of the shipping record", example = "f0a1b2c3-0000-4fgh-ccde-555555555555")
    private UUID id;

    @Schema(description = "UUID of the order this shipment belongs to", example = "c3d4e5f6-0000-4bcd-9abc-222222222222")
    private UUID orderId;

    @Schema(description = "Carrier-assigned tracking code", example = "BR123456789BR")
    private String trackingCode;

    @Schema(description = "Carrier company performing the delivery", example = "Correios")
    private String carrier;

    @Schema(description = "Current shipping status", example = "PROCESSING")
    private ShippingStatus status;

    @Schema(description = "Expected delivery date (yyyy-MM-dd)", example = "2026-08-10")
    private LocalDate estimatedDelivery;

    @Schema(description = "Timestamp when the package was dispatched (null until shipped)", example = "2026-08-01T14:30:00")
    private LocalDateTime shippedAt;

    @Schema(description = "Timestamp when the package was delivered (null until delivered)", example = "2026-08-09T10:15:00")
    private LocalDateTime deliveredAt;
}