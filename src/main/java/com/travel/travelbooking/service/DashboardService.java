package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.*;

import java.io.IOException;
import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getUserStats();
    DashboardStatsDTO getFullDashboardStats(); // ← bắt buộc implement
    List<Object[]> getUserRegistrationLast7Days();
    byte[] exportDashboardToExcel() throws IOException;

    List<PopularDestinationDTO> getTop5PopularDestinationsPublic();
    List<PopularTourDTO> getTop10PopularToursPublic();
    List<TopBookedTourDTO> getTop10MostBookedToursPublic();
    List<LatestTourDTO> getLatestToursPublic(int limit);
}
