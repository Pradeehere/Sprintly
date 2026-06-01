package com.sprintly.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class BoardColumnResponse {
    private UUID id;
    private UUID boardId;
    private String name;
    private int position;
}
