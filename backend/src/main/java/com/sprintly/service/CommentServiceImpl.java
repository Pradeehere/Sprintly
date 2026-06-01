package com.sprintly.service;

import com.sprintly.dto.request.CreateCommentRequest;
import com.sprintly.dto.response.CommentResponse;
import com.sprintly.entity.Comment;
import com.sprintly.entity.Task;
import com.sprintly.entity.User;
import com.sprintly.exception.ResourceNotFoundException;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.CommentRepository;
import com.sprintly.repository.TaskRepository;
import com.sprintly.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request, UUID authorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(request.getContent());
        
        comment = commentRepository.save(comment);
        return genericMapper.toCommentResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getTaskComments(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(genericMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }
}
