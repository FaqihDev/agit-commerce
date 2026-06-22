package com.example.order.controller;

import com.example.order.entity.Notification;
import com.example.order.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getNotificationsByUserId_Success() throws Exception {
        Notification n = new Notification();
        n.setId(1L);
        n.setUserId("user1");
        n.setMessage("Test message");

        when(notificationService.getUserNotifications("user1")).thenReturn(List.of(n));

        mockMvc.perform(get("/notifications").param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].message").value("Test message"));
    }
}
