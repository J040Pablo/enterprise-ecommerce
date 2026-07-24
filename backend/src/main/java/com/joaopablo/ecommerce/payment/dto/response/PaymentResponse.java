package com.joaopablo.ecommerce.payment.dto.response;

import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
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
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
}
