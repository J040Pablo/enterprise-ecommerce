package com.joaopablo.ecommerce.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CreatePaymentRequest", description = "Payload for creating a payment linked to an existing order")
public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    @Schema(
            description = "UUID of the confirmed order to be paid",
            example = "c3d4e5f6-0000-4bcd-9abc-222222222222",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID orderId;

    @NotBlank(message = "Payment method is required")
    @Schema(
            description = "Payment method chosen by the customer (e.g., CREDIT_CARD, PIX, BOLETO)",
            example = "CREDIT_CARD",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String paymentMethod;
}

