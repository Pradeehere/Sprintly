package com.sprintly.dto.response;

import com.sprintly.entity.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private boolean isRead;
    private UUID referenceId;
    private LocalDateTime createdAt;
}
