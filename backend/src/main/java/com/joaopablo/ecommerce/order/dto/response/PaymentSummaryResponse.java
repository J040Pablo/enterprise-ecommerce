package com.joaopablo.ecommerce.order.dto.response;

import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
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
@Schema(name = "PaymentSummaryResponse", description = "Compact payment record embedded in an order response")
public class PaymentSummaryResponse {

    @Schema(description = "Unique identifier of the payment", example = "e7f8a9b0-0000-4efg-bbcd-444444444444")
    private UUID id;

    @Schema(description = "Current payment status", example = "APPROVED")
    private PaymentStatus status;

}