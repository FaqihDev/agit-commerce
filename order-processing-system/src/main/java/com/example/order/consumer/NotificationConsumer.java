package com.example.order.consumer;

import com.example.order.dto.message.OrderEventMessage;
import com.example.order.entity.Order;
import com.example.order.service.NotificationService;
import com.example.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final OrderService orderService;
    private final NotificationService notificationService;

    public NotificationConsumer(OrderService orderService, NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-paid}", ackMode = "MANUAL")
    public void processOrderPaid(OrderEventMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received order.paid for orderId: {}", message.getOrderId());
        try {
            Order order = orderService.getOrderById(message.getOrderId());
            String text = "Order #" + order.getId() + " berhasil dibayar";
            notificationService.createNotification(order.getUserId(), text);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing order.paid for orderId: {}", message.getOrderId(), e);
            channel.basicReject(tag, false);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-failed}", ackMode = "MANUAL")
    public void processOrderFailed(OrderEventMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received order.failed for orderId: {}", message.getOrderId());
        try {
            Order order = orderService.getOrderById(message.getOrderId());
            String text = "Order #" + order.getId() + " gagal diproses";
            notificationService.createNotification(order.getUserId(), text);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing order.failed for orderId: {}", message.getOrderId(), e);
            channel.basicReject(tag, false);
        }
    }
}
