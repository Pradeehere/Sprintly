package com.sprintly.service;

import com.sprintly.dto.request.CreateLabelRequest;
import com.sprintly.dto.response.LabelResponse;
import com.sprintly.entity.Label;
import com.sprintly.entity.Project;
import com.sprintly.exception.ResourceNotFoundException;
import com.sprintly.mapper.GenericMapper;
import com.sprintly.repository.LabelRepository;
import com.sprintly.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public LabelResponse createLabel(UUID projectId, CreateLabelRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
                
        Label label = new Label();
        label.setProject(project);
        label.setName(request.getName());
        label.setColor(request.getColor());
        
        label = labelRepository.save(label);
        return genericMapper.toLabelResponse(label);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelResponse> getProjectLabels(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return labelRepository.findAllByProjectId(projectId).stream()
                .map(genericMapper::toLabelResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLabel(UUID labelId) {
        if (!labelRepository.existsById(labelId)) {
            throw new ResourceNotFoundException("Label not found");
        }
        labelRepository.deleteById(labelId);
    }
}
