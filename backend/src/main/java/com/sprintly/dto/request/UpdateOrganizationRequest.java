package com.sprintly.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrganizationRequest {

    @Size(max = 100, message = "Organization name cannot exceed 100 characters")
    private String name;

    @Size(max = 512, message = "Logo URL cannot exceed 512 characters")
    private String logoUrl;
}
