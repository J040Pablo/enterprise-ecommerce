package com.joaopablo.ecommerce.payment.dto.response;

import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PaymentResponse", description = "Full payment record returned by the Payments API")
public class PaymentResponse {

    @Schema(description = "Unique identifier of the payment", example = "e7f8a9b0-0000-4efg-bbcd-444444444444")
    private UUID id;

    @Schema(description = "UUID of the order this payment is linked to", example = "c3d4e5f6-0000-4bcd-9abc-222222222222")
    private UUID orderId;

    @Schema(description = "Total amount charged for the order", example = "299.97")
    private BigDecimal amount;

    @Schema(description = "Current status of the payment", example = "PENDING")
    private PaymentStatus status;

    @Schema(description = "Payment method used", example = "CREDIT_CARD")
    private String paymentMethod;
}

