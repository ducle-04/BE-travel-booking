package com.travel.travelbooking.Dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalCustomers;
    private long totalStaff;
    private long totalAdmins;
    private long totalInactive;
    private long newUsersToday;
    private long totalDeleted;
}