package com.sprintly.dto.request;

import com.sprintly.entity.enums.OrgRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddOrgMemberRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private OrgRole role;
}
