package com.joaopablo.ecommerce.order.messaging;

import com.joaopablo.ecommerce.common.messaging.rabbitmq.RabbitMqProperties;
import com.joaopablo.ecommerce.common.messaging.rabbitmq.event.OrderCancelledEvent;
import com.joaopablo.ecommerce.common.messaging.rabbitmq.event.OrderCreatedEvent;
import com.joaopablo.ecommerce.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                LocalDateTime.now()
        );

        publishAfterCommit(RabbitMqProperties.ORDER_CREATED_ROUTING_KEY, event);
    }

    public void publishOrderCancelled(Order order) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getId(),
                order.getUserId(),
                LocalDateTime.now()
        );

        publishAfterCommit(RabbitMqProperties.ORDER_CANCELLED_ROUTING_KEY, event);
    }

    private void publishAfterCommit(String routingKey, Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(routingKey, event);
                }
            });
            return;
        }

        publish(routingKey, event);
    }

    private void publish(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqProperties.TOPIC_EXCHANGE, routingKey, event);
            log.info("Published RabbitMQ event routingKey={} payload={}", routingKey, event);
        } catch (AmqpException exception) {
            log.warn("Could not publish RabbitMQ event routingKey={} payload={}", routingKey, event, exception);
        }
    }
}
