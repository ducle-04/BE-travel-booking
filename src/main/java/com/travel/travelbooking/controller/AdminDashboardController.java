package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.DashboardStatsDTO;
import com.travel.travelbooking.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getUserStats());
    }

    @GetMapping("/chart/last-7-days")
    public ResponseEntity<List<Object[]>> getRegistrationChart() {
        return ResponseEntity.ok(dashboardService.getUserRegistrationLast7Days());
    }
}