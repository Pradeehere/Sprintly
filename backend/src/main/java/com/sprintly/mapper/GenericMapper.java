package com.sprintly.mapper;

import com.sprintly.dto.response.*;
import com.sprintly.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GenericMapper {

    public UserSummaryResponse toUserSummaryResponse(User user) {
        if (user == null) return null;
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFullName()) // Mapping fullName to firstName for now, or just mapping to a new field
                .lastName("") // Dummy for now, can be updated later if DTO changes
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public OrganizationResponse toOrganizationResponse(Organization org) {
        if (org == null) return null;
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .logoUrl(org.getLogoUrl())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    public OrganizationMemberResponse toOrgMemberResponse(OrganizationMember member) {
        if (member == null) return null;
        return OrganizationMemberResponse.builder()
                .id(member.getId())
                .organizationId(member.getOrganization().getId())
                .user(toUserSummaryResponse(member.getUser()))
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public ProjectResponse toProjectResponse(Project project) {
        if (project == null) return null;
        return ProjectResponse.builder()
                .id(project.getId())
                .organizationId(project.getOrganization().getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public BoardResponse toBoardResponse(Board board) {
        if (board == null) return null;
        return BoardResponse.builder()
                .id(board.getId())
                .projectId(board.getProject().getId())
                .name(board.getName())
                .build();
    }

    public BoardColumnResponse toBoardColumnResponse(BoardColumn column) {
        if (column == null) return null;
        return BoardColumnResponse.builder()
                .id(column.getId())
                .boardId(column.getBoard().getId())
                .name(column.getName())
                .position(column.getPosition())
                .build();
    }

    public LabelResponse toLabelResponse(Label label) {
        if (label == null) return null;
        return LabelResponse.builder()
                .id(label.getId())
                .projectId(label.getProject().getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }

    public TaskResponse toTaskResponse(Task task) {
        if (task == null) return null;
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .columnId(task.getColumn().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(toUserSummaryResponse(task.getAssignee()))
                .reporter(toUserSummaryResponse(task.getReporter()))
                .position(task.getPosition())
                .dueDate(task.getDueDate())
                .labels(task.getLabels() != null ? 
                        task.getLabels().stream().map(this::toLabelResponse).collect(Collectors.toList()) : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) return null;
        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .author(toUserSummaryResponse(comment.getAuthor()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
