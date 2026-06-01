package com.sprintly.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLabelRequest {

    @NotBlank(message = "Label name is required")
    @Size(max = 100, message = "Label name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Label color is required")
    @Size(max = 20, message = "Label color cannot exceed 20 characters")
    private String color;
}
