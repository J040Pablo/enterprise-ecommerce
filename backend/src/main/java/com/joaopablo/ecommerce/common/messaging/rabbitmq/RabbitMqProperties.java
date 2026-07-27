package com.joaopablo.ecommerce.common.messaging.rabbitmq;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RabbitMqProperties {

    public static final String TOPIC_EXCHANGE = "ecommerce.topic";

    public static final String PAYMENT_ORDER_CREATED_QUEUE = "payment.order-created.queue";
    public static final String ORDER_PAYMENT_RESULT_QUEUE = "order.payment-result.queue";
    public static final String SHIPPING_PAYMENT_APPROVED_QUEUE = "shipping.payment-approved.queue";
    public static final String INVENTORY_EVENTS_QUEUE = "inventory.events.queue";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String PAYMENT_APPROVED_ROUTING_KEY = "payment.approved";
    public static final String PAYMENT_REJECTED_ROUTING_KEY = "payment.rejected";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
}
