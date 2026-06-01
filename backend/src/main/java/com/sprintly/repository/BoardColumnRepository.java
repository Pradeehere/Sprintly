package com.sprintly.repository;

import com.sprintly.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {
    List<BoardColumn> findAllByBoardIdOrderByPositionAsc(UUID boardId);
    int countByBoardId(UUID boardId);
}
