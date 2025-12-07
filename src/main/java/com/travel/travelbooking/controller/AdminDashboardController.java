package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.DashboardStatsDTO;
import com.travel.travelbooking.service.DashboardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @GetMapping("/stats/full")
    public ResponseEntity<DashboardStatsDTO> getFullStats() {
        return ResponseEntity.ok(dashboardService.getFullDashboardStats());
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportDashboardToExcel() throws IOException {
        byte[] excelBytes = dashboardService.exportDashboardToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "Bao_cao_Dashboard_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}