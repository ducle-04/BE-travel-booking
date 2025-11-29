package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.DashboardStatsDTO;
import com.travel.travelbooking.Repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardStatsDTO getUserStats() {
        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalUsers(dashboardRepository.countActiveUsers());
        stats.setTotalCustomers(dashboardRepository.countUsersByRole("USER"));
        stats.setTotalStaff(dashboardRepository.countUsersByRole("STAFF"));
        stats.setTotalAdmins(dashboardRepository.countUsersByRole("ADMIN"));
        stats.setTotalInactive(dashboardRepository.countInactiveOrBanned());
        stats.setNewUsersToday(dashboardRepository.countNewUsersToday(startOfDay));
        stats.setTotalDeleted(dashboardRepository.countDeletedUsers());
        return stats;
    }

    @Override
    public List<Object[]> getUserRegistrationLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return dashboardRepository.countNewUsersLast7Days(sevenDaysAgo);
    }
}