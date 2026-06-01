package com.sprintly.service;

import com.sprintly.dto.request.AddOrgMemberRequest;
import com.sprintly.dto.request.CreateOrganizationRequest;
import com.sprintly.dto.request.UpdateOrganizationRequest;
import com.sprintly.dto.response.OrganizationMemberResponse;
import com.sprintly.dto.response.OrganizationResponse;
import com.sprintly.entity.Organization;
import com.sprintly.entity.OrganizationMember;
import com.sprintly.entity.User;
import com.sprintly.entity.enums.OrgRole;
import com.sprintly.exception.ResourceNotFoundException;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.OrganizationMemberRepository;
import com.sprintly.repository.OrganizationRepository;
import com.sprintly.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, UUID currentUserId) {
        String baseSlug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String slug = baseSlug;
        int count = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count++;
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization org = new Organization();
        org.setName(request.getName());
        org.setSlug(slug);
        org.setLogoUrl(request.getLogoUrl());
        org.setOwner(currentUser);

        org = organizationRepository.save(org);

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(org);
        member.setUser(currentUser);
        member.setRole(OrgRole.ADMIN);
        
        organizationMemberRepository.save(member);

        return genericMapper.toOrganizationResponse(org);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return genericMapper.toOrganizationResponse(org);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(String slug) {
        Organization org = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return genericMapper.toOrganizationResponse(org);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(UUID orgId, UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            org.setName(request.getName());
        }
        if (request.getLogoUrl() != null) {
            org.setLogoUrl(request.getLogoUrl());
        }
        
        org = organizationRepository.save(org);
        return genericMapper.toOrganizationResponse(org);
    }

    @Override
    @Transactional
    public void deleteOrganization(UUID orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        organizationRepository.deleteById(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getUserOrganizations(UUID userId) {
        return organizationRepository.findAllByMemberUserId(userId).stream()
                .map(genericMapper::toOrganizationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrganizationMemberResponse addMember(UUID orgId, AddOrgMemberRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
                
        if (organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, user.getId())) {
            throw new IllegalArgumentException("User is already a member of this organization");
        }

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(org);
        member.setUser(user);
        member.setRole(request.getRole());
        
        member = organizationMemberRepository.save(member);
        return genericMapper.toOrgMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> getOrganizationMembers(UUID orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return organizationMemberRepository.findAllByOrganizationId(orgId).stream()
                .map(genericMapper::toOrgMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(UUID orgId, UUID memberUserId) {
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, memberUserId)) {
            throw new ResourceNotFoundException("Organization member not found");
        }
        organizationMemberRepository.deleteByOrganizationIdAndUserId(orgId, memberUserId);
    }
}
