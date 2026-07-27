package com.joaopablo.ecommerce.common.messaging.rabbitmq.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
