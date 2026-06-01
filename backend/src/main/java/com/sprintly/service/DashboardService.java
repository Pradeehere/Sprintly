package com.sprintly.service;

import com.sprintly.dto.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardResponse getDashboard(UUID userId);
}
