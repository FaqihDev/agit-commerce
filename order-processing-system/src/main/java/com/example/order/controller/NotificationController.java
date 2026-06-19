package com.example.order.controller;

import com.example.order.entity.Notification;
import com.example.order.enums.ResponseCodeEnum;
import com.example.order.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import com.example.order.dto.base.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public BaseResponse<List<Notification>> getUserNotifications(@RequestParam String userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return BaseResponse.success(notifications, ResponseCodeEnum.SUCCESS_RETRIEVE_DATA);
    }
}
