package com.joaopablo.ecommerce.inventory.messaging;

import com.joaopablo.ecommerce.common.messaging.rabbitmq.RabbitMqProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryEventListener {

    @RabbitListener(queues = RabbitMqProperties.INVENTORY_EVENTS_QUEUE)
    public void onInventoryEvent(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String payload = new String(message.getBody());

        log.info("Received inventory-related event. routingKey={} payload={}", routingKey, payload);
    }
}
