package com.sprintly.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class BoardResponse {
    private UUID id;
    private UUID projectId;
    private String name;
}
