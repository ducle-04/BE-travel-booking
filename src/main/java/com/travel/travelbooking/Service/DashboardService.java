package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.DashboardStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getUserStats();
    List<Object[]> getUserRegistrationLast7Days();
}