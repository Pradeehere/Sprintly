package com.sprintly.service;

import com.sprintly.dto.request.CreateTaskRequest;
import com.sprintly.dto.request.MoveTaskRequest;
import com.sprintly.dto.request.UpdateTaskRequest;
import com.sprintly.dto.response.TaskResponse;
import com.sprintly.entity.*;
import com.sprintly.entity.enums.TaskStatus;
import com.sprintly.exception.ResourceNotFoundException;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.*;
import com.sprintly.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final GenericMapper genericMapper;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public TaskResponse createTask(UUID projectId, CreateTaskRequest request, UUID reporterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
                
        BoardColumn column = boardColumnRepository.findById(request.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
                
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId()).orElse(null);
        }

        // Find position
        int maxPosition = taskRepository.countByColumnId(column.getId());

        Task task = new Task();
        task.setProject(project);
        task.setColumn(column);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : com.sprintly.entity.enums.TaskPriority.MEDIUM);
        task.setReporter(reporter);
        task.setAssignee(assignee);
        task.setPosition(maxPosition);
        task.setDueDate(request.getDueDate());
        
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            List<Label> labels = labelRepository.findAllById(request.getLabelIds());
            task.setLabels(labels);
        }

        task = taskRepository.save(task);
        TaskResponse taskResponse = genericMapper.toTaskResponse(task);
        webSocketService.broadcastTaskUpdate(projectId, taskResponse);
        return taskResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return genericMapper.toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
                
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId()).orElse(null);
            task.setAssignee(assignee);
        }
        
        if (request.getLabelIds() != null) {
            List<Label> labels = labelRepository.findAllById(request.getLabelIds());
            task.setLabels(labels);
        }

        task = taskRepository.save(task);
        TaskResponse taskResponse = genericMapper.toTaskResponse(task);
        webSocketService.broadcastTaskUpdate(task.getProject().getId(), taskResponse);
        return taskResponse;
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        taskRepository.deleteById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getProjectTasks(UUID projectId) {
        return taskRepository.findAllByProjectId(projectId).stream()
                .map(genericMapper::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getColumnTasks(UUID columnId) {
        return taskRepository.findAllByColumnIdOrderByPositionAsc(columnId).stream()
                .map(genericMapper::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse moveTask(UUID taskId, MoveTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
                
        BoardColumn targetColumn = boardColumnRepository.findById(request.getTargetColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Target column not found"));

        UUID oldColumnId = task.getColumn().getId();
        UUID newColumnId = targetColumn.getId();
        int oldPosition = task.getPosition();
        int newPosition = request.getPosition();

        // 1. If moving within the same column
        if (oldColumnId.equals(newColumnId)) {
            if (oldPosition == newPosition) {
                return genericMapper.toTaskResponse(task);
            }
            
            List<Task> columnTasks = taskRepository.findAllByColumnIdOrderByPositionAsc(oldColumnId);
            columnTasks.remove(task);
            columnTasks.add(newPosition, task);
            
            // Re-index all positions
            for (int i = 0; i < columnTasks.size(); i++) {
                columnTasks.get(i).setPosition(i);
            }
            taskRepository.saveAll(columnTasks);
        } 
        // 2. If moving to a different column
        else {
            task.setColumn(targetColumn);
            
            // Update the new column's tasks positions
            List<Task> newColumnTasks = taskRepository.findAllByColumnIdOrderByPositionAsc(newColumnId);
            newColumnTasks.add(newPosition, task);
            for (int i = 0; i < newColumnTasks.size(); i++) {
                newColumnTasks.get(i).setPosition(i);
            }
            taskRepository.saveAll(newColumnTasks);
            
            // Re-index old column
            List<Task> oldColumnTasks = taskRepository.findAllByColumnIdOrderByPositionAsc(oldColumnId);
            oldColumnTasks.remove(task);
            for (int i = 0; i < oldColumnTasks.size(); i++) {
                oldColumnTasks.get(i).setPosition(i);
            }
            taskRepository.saveAll(oldColumnTasks);
        }

        TaskResponse taskResponse = genericMapper.toTaskResponse(taskRepository.findById(taskId).get());
        webSocketService.broadcastTaskUpdate(task.getProject().getId(), taskResponse);
        return taskResponse;
    }
}
