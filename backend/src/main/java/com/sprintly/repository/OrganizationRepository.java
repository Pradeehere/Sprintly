package com.sprintly.repository;

import com.sprintly.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT o FROM Organization o JOIN OrganizationMember om ON om.organization = o WHERE om.user.id = :userId")
    List<Organization> findAllByMemberUserId(UUID userId);
}
