package com.sprintly.repository;

import com.sprintly.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    int countByUserIdAndIsReadFalse(UUID userId);
}
