package com.example.order.consumer;

import com.example.order.dto.message.OrderEventMessage;
import com.example.order.entity.Order;
import com.example.order.service.NotificationService;
import com.example.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Channel channel;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void processOrderPaid_Success() throws IOException {
        OrderEventMessage message = new OrderEventMessage(UUID.randomUUID());
        Order order = new Order();
        order.setId(message.getOrderId());
        order.setUserId("user1");

        when(orderService.getOrderById(message.getOrderId())).thenReturn(order);

        notificationConsumer.processOrderPaid(message, channel, 123L);

        verify(notificationService).createNotification(eq("user1"), anyString());
        verify(channel).basicAck(123L, false);
    }

    @Test
    void processOrderPaid_Failed() throws IOException {
        OrderEventMessage message = new OrderEventMessage(UUID.randomUUID());

        when(orderService.getOrderById(message.getOrderId())).thenThrow(new RuntimeException("Not found"));

        notificationConsumer.processOrderPaid(message, channel, 123L);

        verify(notificationService, never()).createNotification(anyString(), anyString());
        verify(channel).basicReject(123L, false);
    }

    @Test
    void processOrderFailed_Success() throws IOException {
        OrderEventMessage message = new OrderEventMessage(UUID.randomUUID());
        Order order = new Order();
        order.setId(message.getOrderId());
        order.setUserId("user1");

        when(orderService.getOrderById(message.getOrderId())).thenReturn(order);

        notificationConsumer.processOrderFailed(message, channel, 123L);

        verify(notificationService).createNotification(eq("user1"), anyString());
        verify(channel).basicAck(123L, false);
    }
}
