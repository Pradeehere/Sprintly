package com.sprintly.controller;

import com.sprintly.dto.request.AddOrgMemberRequest;
import com.sprintly.dto.request.CreateOrganizationRequest;
import com.sprintly.dto.request.UpdateOrganizationRequest;
import com.sprintly.dto.response.OrganizationMemberResponse;
import com.sprintly.dto.response.OrganizationResponse;
import com.sprintly.security.UserPrincipal;
import com.sprintly.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        OrganizationResponse response = organizationService.createOrganization(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getUserOrganizations(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(organizationService.getUserOrganizations(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganizationBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(organizationService.getOrganizationBySlug(slug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    // Members endpoints
    @GetMapping("/{id}/members")
    public ResponseEntity<List<OrganizationMemberResponse>> getOrganizationMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationMembers(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<OrganizationMemberResponse> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddOrgMemberRequest request) {
        OrganizationMemberResponse response = organizationService.addMember(id, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        organizationService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }
}
