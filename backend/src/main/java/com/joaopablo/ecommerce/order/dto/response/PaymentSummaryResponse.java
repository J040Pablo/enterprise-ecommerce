package com.joaopablo.ecommerce.order.dto.response;

import com.joaopablo.ecommerce.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {

    private UUID id;
    private PaymentStatus status;

}