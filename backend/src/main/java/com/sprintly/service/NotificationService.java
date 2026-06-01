package com.sprintly.service;

import com.sprintly.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationResponse> getUserNotifications(UUID userId);
    List<NotificationResponse> getUnreadNotifications(UUID userId);
    int getUnreadCount(UUID userId);
    void markAsRead(UUID notificationId);
    void markAllAsRead(UUID userId);
}
