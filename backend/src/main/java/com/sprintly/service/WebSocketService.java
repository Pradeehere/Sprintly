package com.sprintly.service;

import com.sprintly.dto.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastTaskUpdate(UUID projectId, TaskResponse task) {
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/tasks", task);
    }
}
