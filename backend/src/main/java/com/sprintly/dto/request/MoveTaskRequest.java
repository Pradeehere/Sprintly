package com.sprintly.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MoveTaskRequest {

    @NotNull(message = "Target column ID is required")
    private UUID targetColumnId;

    @NotNull(message = "New position is required")
    private int position;
}
