package com.sprintly.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatarUrl;
}
