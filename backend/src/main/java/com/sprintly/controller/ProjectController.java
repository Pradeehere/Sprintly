package com.sprintly.controller;

import com.sprintly.dto.request.AddProjectMemberRequest;
import com.sprintly.dto.request.CreateProjectRequest;
import com.sprintly.dto.request.UpdateProjectRequest;
import com.sprintly.dto.response.ProjectResponse;
import com.sprintly.security.UserPrincipal;
import com.sprintly.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/organizations/{orgId}/projects")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse response = projectService.createProject(orgId, request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/organizations/{orgId}/projects")
    public ResponseEntity<List<ProjectResponse>> getOrganizationProjects(@PathVariable UUID orgId) {
        return ResponseEntity.ok(projectService.getOrganizationProjects(orgId));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // Members
    @PostMapping("/projects/{id}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddProjectMemberRequest request) {
        projectService.addMember(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/projects/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }
}
