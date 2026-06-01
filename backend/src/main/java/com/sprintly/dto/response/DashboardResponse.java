package com.sprintly.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DashboardResponse {
    private int totalOrganizations;
    private int totalProjects;
    private int totalTasks;
    private int tasksAssignedToMe;
    private int tasksTodo;
    private int tasksInProgress;
    private int tasksDone;
    private List<ProjectResponse> recentProjects;
}
