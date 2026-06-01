package com.sprintly.controller;

import com.sprintly.dto.request.CreateTaskRequest;
import com.sprintly.dto.request.MoveTaskRequest;
import com.sprintly.dto.request.UpdateTaskRequest;
import com.sprintly.dto.response.TaskResponse;
import com.sprintly.security.UserPrincipal;
import com.sprintly.service.TaskService;
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
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TaskResponse response = taskService.createTask(projectId, request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getProjectTasks(projectId));
    }

    @GetMapping("/columns/{columnId}/tasks")
    public ResponseEntity<List<TaskResponse>> getColumnTasks(@PathVariable UUID columnId) {
        return ResponseEntity.ok(taskService.getColumnTasks(columnId));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PutMapping("/tasks/{id}/move")
    public ResponseEntity<TaskResponse> moveTask(
            @PathVariable UUID id,
            @Valid @RequestBody MoveTaskRequest request) {
        return ResponseEntity.ok(taskService.moveTask(id, request));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
