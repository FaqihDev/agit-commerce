package com.example.order.producer;

import com.example.order.dto.message.OrderEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing-keys.order-paid}")
    private String orderPaidRoutingKey;

    @Value("${rabbitmq.routing-keys.order-failed}")
    private String orderFailedRoutingKey;

    public OrderEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderEventMessage message) {
        log.info("Publishing order.created event for orderId: {}", message.getOrderId());
        rabbitTemplate.convertAndSend(exchange, orderCreatedRoutingKey, message);
    }

    public void publishOrderPaid(OrderEventMessage message) {
        log.info("Publishing order.paid event for orderId: {}", message.getOrderId());
        rabbitTemplate.convertAndSend(exchange, orderPaidRoutingKey, message);
    }

    public void publishOrderFailed(OrderEventMessage message) {
        log.info("Publishing order.failed event for orderId: {}", message.getOrderId());
        rabbitTemplate.convertAndSend(exchange, orderFailedRoutingKey, message);
    }
}
