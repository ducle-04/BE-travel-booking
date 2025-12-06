package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.DashboardStatsDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getUserStats();
    DashboardStatsDTO getFullDashboardStats(); // ← bắt buộc implement
    List<Object[]> getUserRegistrationLast7Days();
}
