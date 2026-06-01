package com.sprintly.service;

import com.sprintly.dto.request.CreateTaskRequest;
import com.sprintly.dto.request.MoveTaskRequest;
import com.sprintly.dto.request.UpdateTaskRequest;
import com.sprintly.dto.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(UUID projectId, CreateTaskRequest request, UUID reporterId);
    TaskResponse getTaskById(UUID taskId);
    TaskResponse updateTask(UUID taskId, UpdateTaskRequest request);
    void deleteTask(UUID taskId);
    List<TaskResponse> getProjectTasks(UUID projectId);
    List<TaskResponse> getColumnTasks(UUID columnId);
    TaskResponse moveTask(UUID taskId, MoveTaskRequest request);
}
