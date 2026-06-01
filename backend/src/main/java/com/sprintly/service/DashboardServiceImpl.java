package com.sprintly.service;

import com.sprintly.dto.response.DashboardResponse;
import com.sprintly.entity.enums.TaskStatus;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.OrganizationRepository;
import com.sprintly.repository.ProjectRepository;
import com.sprintly.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DashboardServiceImpl implements DashboardService {

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID userId) {
        var orgs = organizationRepository.findAllByMemberUserId(userId);
        int totalOrgs = orgs.size();

        var allProjects = orgs.stream()
                .flatMap(org -> projectRepository.findAllByOrganizationId(org.getId()).stream())
                .collect(Collectors.toList());
        int totalProjects = allProjects.size();

        var allTasks = allProjects.stream()
                .flatMap(project -> taskRepository.findAllByProjectId(project.getId()).stream())
                .collect(Collectors.toList());

        int totalTasks = allTasks.size();
        int assignedToMe = (int) allTasks.stream()
                .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(userId))
                .count();
        int tasksTodo = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.TODO).count();
        int tasksInProgress = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int tasksDone = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE).count();

        var recentProjects = allProjects.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(genericMapper::toProjectResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalOrganizations(totalOrgs)
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .tasksAssignedToMe(assignedToMe)
                .tasksTodo(tasksTodo)
                .tasksInProgress(tasksInProgress)
                .tasksDone(tasksDone)
                .recentProjects(recentProjects)
                .build();
    }
}
