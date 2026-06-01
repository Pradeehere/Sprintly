package com.sprintly.service;

import com.sprintly.dto.request.CreateCommentRequest;
import com.sprintly.dto.response.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse createComment(UUID taskId, CreateCommentRequest request, UUID authorId);
    List<CommentResponse> getTaskComments(UUID taskId);
    void deleteComment(UUID commentId);
}
