package com.joaopablo.ecommerce.payment.messaging;

import com.joaopablo.ecommerce.common.messaging.rabbitmq.RabbitMqProperties;
import com.joaopablo.ecommerce.common.messaging.rabbitmq.event.PaymentApprovedEvent;
import com.joaopablo.ecommerce.common.messaging.rabbitmq.event.PaymentRejectedEvent;
import com.joaopablo.ecommerce.payment.entity.Payment;
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
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentApproved(Payment payment) {
        PaymentApprovedEvent event = new PaymentApprovedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                LocalDateTime.now()
        );

        publishAfterCommit(RabbitMqProperties.PAYMENT_APPROVED_ROUTING_KEY, event);
    }

    public void publishPaymentRejected(Payment payment) {
        PaymentRejectedEvent event = new PaymentRejectedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                LocalDateTime.now()
        );

        publishAfterCommit(RabbitMqProperties.PAYMENT_REJECTED_ROUTING_KEY, event);
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
