package com.sprintly.dto.response;

import com.sprintly.entity.enums.OrgRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrganizationMemberResponse {
    private UUID id;
    private UUID organizationId;
    private UserSummaryResponse user;
    private OrgRole role;
    private LocalDateTime joinedAt;
}
