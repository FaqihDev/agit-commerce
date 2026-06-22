package com.example.order.service;

import com.example.order.entity.Notification;
import com.example.order.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_Success() {
        notificationService.createNotification("user1", "Test Message");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_Success() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUserId("user1");
        n.setMessage("Test Message");

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user1")).thenReturn(List.of(n));

        List<Notification> result = notificationService.getUserNotifications("user1");

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUserId());
    }
}
