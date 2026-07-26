package com.joaopablo.ecommerce.shipping.dto.response;

import com.joaopablo.ecommerce.shipping.entity.ShippingStatus;
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
public class ShippingResponse {

    private UUID id;
    private UUID orderId;
    private String trackingCode;
    private String carrier;
    private ShippingStatus status;
    private LocalDate estimatedDelivery;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}