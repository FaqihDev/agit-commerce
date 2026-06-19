package com.example.order.consumer;

import com.example.order.dto.message.OrderEventMessage;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.enums.OrderStatusEnum;
import com.example.order.producer.OrderEventProducer;
import com.example.order.repository.OrderItemRepository;
import com.example.order.service.OrderService;
import com.example.order.service.ProductService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class OrderProcessorConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessorConsumer.class);

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderEventProducer orderEventProducer;
    private final OrderItemRepository orderItemRepository;

    public OrderProcessorConsumer(OrderService orderService, ProductService productService,
                                  OrderEventProducer orderEventProducer, OrderItemRepository orderItemRepository) {
        this.orderService = orderService;
        this.productService = productService;
        this.orderEventProducer = orderEventProducer;
        this.orderItemRepository = orderItemRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-created}", ackMode = "MANUAL")
    public void processOrder(OrderEventMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received order.created for orderId: {}", message.getOrderId());
        try {
            Order order = orderService.getOrderById(message.getOrderId());

            // If order isn't PENDING, ignore (could be duplicate)
            if (!OrderStatusEnum.PENDING.name().equals(order.getStatus())) {
                channel.basicAck(tag, false);
                return;
            }

            processPaymentAndInventory(order);

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Order processing failed for orderId: {}. Stok tidak tersedia atau error lain. Mengubah status menjadi FAILED. Reason: {}", message.getOrderId(), e.getMessage());
            // Update order status to FAILED
            try {
                orderService.updateOrderStatus(message.getOrderId(), OrderStatusEnum.FAILED.name());
                orderEventProducer.publishOrderFailed(new OrderEventMessage(message.getOrderId()));
            } catch (Exception ex) {
                log.error("Failed to update status to FAILED for orderId: {}", message.getOrderId(), ex);
            }
            channel.basicReject(tag, false); // Send to DLQ
        }
    }

    private void processPaymentAndInventory(Order order) {
        try {
            // Assume OrderItemRepository has findByOrderId which we'll add
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                productService.reduceStock(item.getProductId(), item.getQuantity());
            }
            orderService.updateOrderStatus(order.getId(), OrderStatusEnum.PAID.name());
            orderEventProducer.publishOrderPaid(new OrderEventMessage(order.getId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
