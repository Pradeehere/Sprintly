package com.sprintly.controller;

import com.sprintly.dto.request.CreateLabelRequest;
import com.sprintly.dto.response.LabelResponse;
import com.sprintly.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/projects/{projectId}/labels")
    public ResponseEntity<LabelResponse> createLabel(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateLabelRequest request) {
        LabelResponse response = labelService.createLabel(projectId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/labels")
    public ResponseEntity<List<LabelResponse>> getProjectLabels(@PathVariable UUID projectId) {
        return ResponseEntity.ok(labelService.getProjectLabels(projectId));
    }

    @DeleteMapping("/labels/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable UUID id) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }
}
