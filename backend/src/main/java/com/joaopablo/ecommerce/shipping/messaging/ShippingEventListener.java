package com.joaopablo.ecommerce.shipping.messaging;

import com.joaopablo.ecommerce.common.messaging.rabbitmq.RabbitMqProperties;
import com.joaopablo.ecommerce.common.messaging.rabbitmq.event.PaymentApprovedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShippingEventListener {

    @RabbitListener(queues = RabbitMqProperties.SHIPPING_PAYMENT_APPROVED_QUEUE)
    public void onPaymentApproved(PaymentApprovedEvent event) {
        log.info("Received payment.approved event in shipping module. event={}", event);
    }
}
