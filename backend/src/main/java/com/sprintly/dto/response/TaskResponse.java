package com.sprintly.dto.response;

import com.sprintly.entity.enums.TaskPriority;
import com.sprintly.entity.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TaskResponse {
    private UUID id;
    private UUID projectId;
    private UUID columnId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UserSummaryResponse assignee;
    private UserSummaryResponse reporter;
    private int position;
    private LocalDate dueDate;
    private List<LabelResponse> labels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
