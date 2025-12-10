package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.TourStartDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourStartDateRepository extends JpaRepository<TourStartDate, Long> {

    List<TourStartDate> findByTourIdIn(List<Long> tourIds);

    // Lấy danh sách ngày (LocalDate) theo tour
    @Query("SELECT tsd.startDate FROM TourStartDate tsd WHERE tsd.tour.id = :tourId ORDER BY tsd.startDate")
    List<LocalDate> findStartDatesByTourId(@Param("tourId") Long tourId);


    // Lấy entity ngày khởi hành theo tour + ngày
    @Query("SELECT tsd FROM TourStartDate tsd WHERE tsd.tour.id = :tourId AND tsd.startDate = :date")
    Optional<TourStartDate> findByTourIdAndStartDate(@Param("tourId") Long tourId, @Param("date") LocalDate date);

    // ⭐ Thêm hàm cần thiết cho GroqChatService (FULL ENTITY & SORTED)
    List<TourStartDate> findByTourIdOrderByStartDate(Long tourId);

    // Kiểm tra tồn tại booking theo ngày (dùng trong Update Tour)
    @Query("""
        SELECT COUNT(b) > 0 
        FROM Booking b 
        WHERE b.selectedStartDate.id = :startDateId
          AND b.status IN ('PENDING','CONFIRMED','COMPLETED')
    """)
    boolean existsBySelectedStartDateId(@Param("startDateId") Long startDateId);
}
