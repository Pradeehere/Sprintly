package com.sprintly.controller;

import com.sprintly.dto.response.ApiResponse;
import com.sprintly.dto.response.DashboardResponse;
import com.sprintly.security.UserPrincipal;
import com.sprintly.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardResponse response = dashboardService.getDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved", response));
    }
}
