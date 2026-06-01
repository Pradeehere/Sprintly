package com.sprintly.service;

import com.sprintly.dto.request.AddProjectMemberRequest;
import com.sprintly.dto.request.CreateProjectRequest;
import com.sprintly.dto.request.UpdateProjectRequest;
import com.sprintly.dto.response.ProjectResponse;
import com.sprintly.entity.*;
import com.sprintly.entity.enums.ProjRole;
import com.sprintly.entity.enums.ProjectStatus;
import com.sprintly.exception.ResourceNotFoundException;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(UUID organizationId, CreateProjectRequest request, UUID currentUserId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Project project = new Project();
        project.setOrganization(org);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.ACTIVE);

        project = projectRepository.save(project);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(currentUser);
        member.setRole(ProjRole.PROJECT_MANAGER);
        
        projectMemberRepository.save(member);
        
        // Create default board
        Board defaultBoard = new Board();
        defaultBoard.setProject(project);
        defaultBoard.setName("Main Board");
        boardRepository.save(defaultBoard);

        // Create default columns
        String[] columnNames = {"To Do", "In Progress", "Done"};
        for (int i = 0; i < columnNames.length; i++) {
            BoardColumn column = new BoardColumn();
            column.setBoard(defaultBoard);
            column.setName(columnNames[i]);
            column.setPosition(i);
            boardColumnRepository.save(column);
        }

        return genericMapper.toProjectResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return genericMapper.toProjectResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        
        project = projectRepository.save(project);
        return genericMapper.toProjectResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        projectRepository.deleteById(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getOrganizationProjects(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return projectRepository.findAllByOrganizationId(organizationId).stream()
                .map(genericMapper::toProjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(UUID projectId, AddProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
                
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.getRole());
        
        projectMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID projectId, UUID memberUserId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, memberUserId)) {
            throw new ResourceNotFoundException("Project member not found");
        }
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, memberUserId);
    }
}
