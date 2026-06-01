package com.sprintly.dto.request;

import com.sprintly.entity.enums.TaskPriority;
import com.sprintly.entity.enums.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateTaskRequest {

    @Size(max = 255, message = "Task title cannot exceed 255 characters")
    private String title;

    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private UUID assigneeId;
    private LocalDate dueDate;
    private List<UUID> labelIds;
}
