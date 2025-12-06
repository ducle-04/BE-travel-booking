package com.travel.travelbooking.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsDTO {
    // === USER STATS
    private long totalUsers;
    private long totalCustomers;
    private long totalStaff;
    private long totalAdmins;
    private long totalInactive;
    private long newUsersToday;
    private long totalDeleted;

    // === TOUR STATS
    private long totalTours;
    private long activeTours;
    private long totalConfirmedBookings;
    private long totalReviews;
    private long totalViews;

    // Top 10 tour phổ biến nhất (dựa trên views + bookings + reviews)
    private List<PopularTourDTO> topPopularTours;

    // Top 5 tour đặt nhiều nhất
    private List<TopBookedTourDTO> top5BookedTours;

    // Số lượng tour theo điểm đến
    private List<Object[]> destinationStatsByRegion;      // [Region, count]
    // Top 5 điểm đến phổ biên
    private List<PopularDestinationDTO> top5PopularDestinations;

    // 5 đơn đặt tour gần nhất
    private List<LatestBookingDTO> latestBookings;

    private Double actualRevenue;     // Doanh thu thực tế
    private Double expectedRevenue;   // Doanh thu dự kiến

    private List<LatestTourDTO> latestTours;
}