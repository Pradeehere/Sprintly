package com.sprintly.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBoardRequest {

    @NotBlank(message = "Board name is required")
    @Size(max = 100, message = "Board name cannot exceed 100 characters")
    private String name;
}
