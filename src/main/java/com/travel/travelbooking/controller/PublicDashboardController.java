package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.*;
import com.travel.travelbooking.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class PublicDashboardController {

    private final DashboardService dashboardService;

    public PublicDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1. TOP 5 ĐIỂM ĐẾN PHỔ BIẾN
    @GetMapping("/top-destinations")
    public ResponseEntity<List<PopularDestinationDTO>> topDestinations() {
        return ResponseEntity.ok(dashboardService.getTop5PopularDestinationsPublic());
    }

    // 2. TOP 10 TOUR PHỔ BIẾN NHẤT
    @GetMapping("/top-popular-tours")
    public ResponseEntity<List<PopularTourDTO>> topPopularTours() {
        return ResponseEntity.ok(dashboardService.getTop10PopularToursPublic());
    }

    // 3. TOP 10 TOUR ĐẶT NHIỀU NHẤT
    @GetMapping("/top-booked-tours")
    public ResponseEntity<List<TopBookedTourDTO>> topBookedTours() {
        return ResponseEntity.ok(dashboardService.getTop10MostBookedToursPublic());
    }

    // 4 10 tour đặt gần nhất
    @GetMapping("/latest-tours")
    public ResponseEntity<List<LatestTourDTO>> getLatestTours(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getLatestToursPublic(limit));
    }
}
