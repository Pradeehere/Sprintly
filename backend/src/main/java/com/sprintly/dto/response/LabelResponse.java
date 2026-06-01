package com.sprintly.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class LabelResponse {
    private UUID id;
    private UUID projectId;
    private String name;
    private String color;
}
