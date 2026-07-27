package com.joaopablo.ecommerce.common.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange ecommerceTopicExchange() {
        return new TopicExchange(RabbitMqProperties.TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentOrderCreatedQueue() {
        return new Queue(RabbitMqProperties.PAYMENT_ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue orderPaymentResultQueue() {
        return new Queue(RabbitMqProperties.ORDER_PAYMENT_RESULT_QUEUE, true);
    }

    @Bean
    public Queue shippingPaymentApprovedQueue() {
        return new Queue(RabbitMqProperties.SHIPPING_PAYMENT_APPROVED_QUEUE, true);
    }

    @Bean
    public Queue inventoryEventsQueue() {
        return new Queue(RabbitMqProperties.INVENTORY_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding bindPaymentOrderCreatedQueue(
            Queue paymentOrderCreatedQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(paymentOrderCreatedQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindOrderPaymentApprovedQueue(
            Queue orderPaymentResultQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(orderPaymentResultQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.PAYMENT_APPROVED_ROUTING_KEY);
    }

    @Bean
    public Binding bindOrderPaymentRejectedQueue(
            Queue orderPaymentResultQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(orderPaymentResultQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.PAYMENT_REJECTED_ROUTING_KEY);
    }

    @Bean
    public Binding bindShippingPaymentApprovedQueue(
            Queue shippingPaymentApprovedQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(shippingPaymentApprovedQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.PAYMENT_APPROVED_ROUTING_KEY);
    }

    @Bean
    public Binding bindInventoryOrderCreatedQueue(
            Queue inventoryEventsQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(inventoryEventsQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindInventoryPaymentRejectedQueue(
            Queue inventoryEventsQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(inventoryEventsQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.PAYMENT_REJECTED_ROUTING_KEY);
    }

    @Bean
    public Binding bindInventoryOrderCancelledQueue(
            Queue inventoryEventsQueue,
            TopicExchange ecommerceTopicExchange
    ) {
        return BindingBuilder.bind(inventoryEventsQueue)
                .to(ecommerceTopicExchange)
                .with(RabbitMqProperties.ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
