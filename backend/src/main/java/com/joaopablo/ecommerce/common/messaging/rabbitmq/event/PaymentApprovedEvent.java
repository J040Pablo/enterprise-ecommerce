package com.joaopablo.ecommerce.common.messaging.rabbitmq.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentApprovedEvent(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        LocalDateTime approvedAt
) {
}
