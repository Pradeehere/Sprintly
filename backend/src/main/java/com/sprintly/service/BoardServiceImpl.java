package com.sprintly.service;

import com.sprintly.dto.request.CreateBoardRequest;
import com.sprintly.dto.response.BoardResponse;
import com.sprintly.entity.Board;
import com.sprintly.entity.Project;
import com.sprintly.exception.ResourceNotFoundException;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.BoardRepository;
import com.sprintly.repository.BoardColumnRepository;
import com.sprintly.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final ProjectRepository projectRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public BoardResponse createBoard(UUID projectId, CreateBoardRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Board board = new Board();
        board.setProject(project);
        board.setName(request.getName());
        
        board = boardRepository.save(board);
        return genericMapper.toBoardResponse(board);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoardById(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        return genericMapper.toBoardResponse(board);
    }

    @Override
    @Transactional
    public void deleteBoard(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
        boardRepository.deleteById(boardId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getProjectBoards(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return boardRepository.findAllByProjectId(projectId).stream()
                .map(genericMapper::toBoardResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sprintly.dto.response.BoardColumnResponse> getBoardColumns(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
        return boardColumnRepository.findAllByBoardIdOrderByPositionAsc(boardId).stream()
                .map(genericMapper::toBoardColumnResponse)
                .collect(Collectors.toList());
    }
}
