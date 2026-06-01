package com.sprintly.controller;

import com.sprintly.dto.request.CreateBoardRequest;
import com.sprintly.dto.response.BoardResponse;
import com.sprintly.dto.response.BoardColumnResponse;
import com.sprintly.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/projects/{projectId}/boards")
    public ResponseEntity<BoardResponse> createBoard(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateBoardRequest request) {
        BoardResponse response = boardService.createBoard(projectId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/boards")
    public ResponseEntity<List<BoardResponse>> getProjectBoards(@PathVariable UUID projectId) {
        return ResponseEntity.ok(boardService.getProjectBoards(projectId));
    }

    @GetMapping("/boards/{id}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable UUID id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }

    @GetMapping("/boards/{id}/columns")
    public ResponseEntity<List<BoardColumnResponse>> getBoardColumns(@PathVariable UUID id) {
        return ResponseEntity.ok(boardService.getBoardColumns(id));
    }

    @DeleteMapping("/boards/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable UUID id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
