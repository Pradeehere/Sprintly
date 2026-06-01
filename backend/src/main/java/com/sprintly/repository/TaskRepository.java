package com.sprintly.repository;

import com.sprintly.entity.Task;
import com.sprintly.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByColumnIdOrderByPositionAsc(UUID columnId);
    List<Task> findAllByProjectId(UUID projectId);
    int countByColumnId(UUID columnId);
    int countByProjectIdAndStatus(UUID projectId, TaskStatus status);
    int countByProjectId(UUID projectId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId AND t.dueDate < :date AND t.status != 'DONE'")
    List<Task> findOverdueTasksByAssignee(UUID userId, LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.project.organization.id = :orgId AND t.dueDate < :date AND t.status != 'DONE'")
    List<Task> findOverdueTasksByOrganization(UUID orgId, LocalDate date);
}
