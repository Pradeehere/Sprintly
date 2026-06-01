package com.sprintly.dto.response;

import com.sprintly.entity.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProjectResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
