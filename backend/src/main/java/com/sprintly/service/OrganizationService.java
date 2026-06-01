package com.sprintly.service;

import com.sprintly.dto.request.AddOrgMemberRequest;
import com.sprintly.dto.request.CreateOrganizationRequest;
import com.sprintly.dto.request.UpdateOrganizationRequest;
import com.sprintly.dto.response.OrganizationMemberResponse;
import com.sprintly.dto.response.OrganizationResponse;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    OrganizationResponse createOrganization(CreateOrganizationRequest request, UUID currentUserId);
    OrganizationResponse getOrganizationById(UUID orgId);
    OrganizationResponse getOrganizationBySlug(String slug);
    OrganizationResponse updateOrganization(UUID orgId, UpdateOrganizationRequest request);
    void deleteOrganization(UUID orgId);
    List<OrganizationResponse> getUserOrganizations(UUID userId);
    
    // Members
    OrganizationMemberResponse addMember(UUID orgId, AddOrgMemberRequest request);
    List<OrganizationMemberResponse> getOrganizationMembers(UUID orgId);
    void removeMember(UUID orgId, UUID memberUserId);
}
