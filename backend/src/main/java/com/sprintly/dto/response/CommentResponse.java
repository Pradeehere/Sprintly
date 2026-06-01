package com.sprintly.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CommentResponse {
    private UUID id;
    private UUID taskId;
    private UserSummaryResponse author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
