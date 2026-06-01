package com.sprintly.dto.request;

import com.sprintly.entity.enums.ProjectStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectRequest {

    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    private String name;

    private String description;
    private ProjectStatus status;
}
