package com.joaopablo.ecommerce.order.dto.response;

import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ShippingSummaryResponse", description = "Compact shipping record embedded in an order response")
public class ShippingSummaryResponse {

    @Schema(description = "Unique identifier of the shipment", example = "f0a1b2c3-0000-4fgh-ccde-555555555555")
    private UUID id;

    @Schema(description = "Carrier tracking code for the package", example = "BR123456789BR")
    private String trackingCode;

    @Schema(description = "Current shipping status", example = "PROCESSING")
    private ShippingStatus status;

}
