package com.sprintly.service;

import com.sprintly.dto.request.CreateLabelRequest;
import com.sprintly.dto.response.LabelResponse;

import java.util.List;
import java.util.UUID;

public interface LabelService {
    LabelResponse createLabel(UUID projectId, CreateLabelRequest request);
    List<LabelResponse> getProjectLabels(UUID projectId);
    void deleteLabel(UUID labelId);
}
