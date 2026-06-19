package com.example.order.service;

import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.dto.message.OrderEventMessage;
import com.example.order.dto.request.OrderItemRequest;
import com.example.order.dto.response.OrderDetailResponse;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.entity.Product;
import com.example.order.enums.ResponseCodeEnum;
import com.example.order.exception.BusinessException;
import com.example.order.producer.OrderEventProducer;
import com.example.order.repository.OrderItemRepository;
import com.example.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        ProductService productService, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Validation: product exists
        for (OrderItemRequest item : request.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new BusinessException(ResponseCodeEnum.INVALID_QUANTITY);
            }
            Product product = productService.getProductById(item.getProductId());
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // Create Order with PENDING status
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(com.example.order.enums.OrderStatusEnum.PENDING.name());
        order = orderRepository.save(order);

        // Save Order Items
        for (OrderItemRequest item : request.getItems()) {
            Product product = productService.getProductById(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItemRepository.save(orderItem);
        }

        // Publish event to RabbitMQ
        orderEventProducer.publishOrderCreated(new OrderEventMessage(order.getId()));

        return order;
    }

    public Order getOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.ORDER_NOT_FOUND));
    }

    @Transactional
    public void updateOrderStatus(UUID id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        orderRepository.save(order);
    }

    public OrderDetailResponse getOrderWithItems(UUID orderId) {
        Order order = getOrderById(orderId);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return new OrderDetailResponse(order, items);
    }
}
