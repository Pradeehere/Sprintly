package com.sprintly.service;

import com.sprintly.dto.request.AddProjectMemberRequest;
import com.sprintly.dto.request.CreateProjectRequest;
import com.sprintly.dto.request.UpdateProjectRequest;
import com.sprintly.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(UUID organizationId, CreateProjectRequest request, UUID currentUserId);
    ProjectResponse getProjectById(UUID projectId);
    ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request);
    void deleteProject(UUID projectId);
    List<ProjectResponse> getOrganizationProjects(UUID organizationId);
    
    void addMember(UUID projectId, AddProjectMemberRequest request);
    void removeMember(UUID projectId, UUID memberUserId);
}
