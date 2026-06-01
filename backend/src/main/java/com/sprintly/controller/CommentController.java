package com.sprintly.controller;

import com.sprintly.dto.request.CreateCommentRequest;
import com.sprintly.dto.response.CommentResponse;
import com.sprintly.security.UserPrincipal;
import com.sprintly.service.CommentService;
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
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CommentResponse response = commentService.createComment(taskId, request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<List<CommentResponse>> getTaskComments(@PathVariable UUID taskId) {
        return ResponseEntity.ok(commentService.getTaskComments(taskId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
