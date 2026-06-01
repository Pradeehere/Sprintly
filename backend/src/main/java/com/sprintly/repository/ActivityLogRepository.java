package com.sprintly.repository;

import com.sprintly.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();

    List<ActivityLog> findAllByPerformedByIdOrderByCreatedAtDesc(UUID userId);
}
