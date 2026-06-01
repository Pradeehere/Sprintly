package com.sprintly.service;

import com.sprintly.dto.request.CreateBoardRequest;
import com.sprintly.dto.response.BoardResponse;

import java.util.List;
import java.util.UUID;

public interface BoardService {
    BoardResponse createBoard(UUID projectId, CreateBoardRequest request);
    BoardResponse getBoardById(UUID boardId);
    void deleteBoard(UUID boardId);
    List<BoardResponse> getProjectBoards(UUID projectId);
    List<com.sprintly.dto.response.BoardColumnResponse> getBoardColumns(UUID boardId);
}
