package com.joaopablo.ecommerce.payment.messaging;

import com.joaopablo.ecommerce.common.messaging.rabbitmq.RabbitMqProperties;
import com.joaopablo.ecommerce.common.messaging.rabbitmq.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventListener {

    @RabbitListener(queues = RabbitMqProperties.PAYMENT_ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event in payment module. event={}", event);
    }
}
