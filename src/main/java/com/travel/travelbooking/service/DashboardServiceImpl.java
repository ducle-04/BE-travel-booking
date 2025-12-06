package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.DashboardStatsDTO;
import com.travel.travelbooking.dto.PopularTourDTO;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.DashboardRepository;
import com.travel.travelbooking.repository.DestinationRepository;
import com.travel.travelbooking.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private  final BookingRepository bookingRepository;

    @Override
    public DashboardStatsDTO getUserStats() {
        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        DashboardStatsDTO stats = new DashboardStatsDTO();

        // === USER STATS ===
        stats.setTotalUsers(dashboardRepository.countActiveUsers());
        stats.setTotalCustomers(dashboardRepository.countUsersByRole("USER"));
        stats.setTotalStaff(dashboardRepository.countUsersByRole("STAFF"));
        stats.setTotalAdmins(dashboardRepository.countUsersByRole("ADMIN"));
        stats.setTotalInactive(dashboardRepository.countInactiveOrBanned());
        stats.setNewUsersToday(dashboardRepository.countNewUsersToday(startOfDay));
        stats.setTotalDeleted(dashboardRepository.countDeletedUsers());

        // === TOUR STATS ===
        var tourStatsData = tourRepository.getTourStats();
        stats.setTotalTours(tourStatsData.getTotalTours());
        stats.setActiveTours(tourStatsData.getActiveTours());
        stats.setTotalConfirmedBookings(tourStatsData.getTotalBookings());

        // Tổng lượt xem + tổng số reviews
        var allTours = tourRepository.findAll();

        stats.setTotalReviews(dashboardRepository.countTotalReviews());

        // Top 10 tour phổ biến
        List<PopularTourDTO> popularTours = tourRepository.findTop10PopularTours();
        stats.setTopPopularTours(popularTours);

        stats.setTop5BookedTours(tourRepository.findTop5BookedTours());

        stats.setDestinationStatsByRegion(destinationRepository.countDestinationsByRegion());
        stats.setTop5PopularDestinations(destinationRepository.findTop5PopularDestinations());

        stats.setLatestBookings(bookingRepository.findTop5LatestBookings());

        stats.setActualRevenue(bookingRepository.getActualRevenue());
        stats.setExpectedRevenue(bookingRepository.getExpectedRevenue());
        stats.setLatestTours(tourRepository.findTop10LatestTours());
        return stats;
    }

    @Override
    public DashboardStatsDTO getFullDashboardStats() {
        // Hiện tại nếu chưa cần thêm gì khác, cứ dùng lại getUserStats()
        return getUserStats();
    }

    @Override
    public List<Object[]> getUserRegistrationLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return dashboardRepository.countNewUsersLast7Days(sevenDaysAgo);
    }
}
