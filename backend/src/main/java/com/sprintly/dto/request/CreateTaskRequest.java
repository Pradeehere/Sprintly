package com.sprintly.dto.request;

import com.sprintly.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title cannot exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Column ID is required")
    private UUID columnId;

    private TaskPriority priority;
    private UUID assigneeId;
    private LocalDate dueDate;
    private List<UUID> labelIds;
}
