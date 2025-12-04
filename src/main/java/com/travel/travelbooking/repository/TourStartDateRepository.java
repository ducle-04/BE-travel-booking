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

    // Hoặc để lấy nhanh LocalDate
    @Query("SELECT tsd.startDate FROM TourStartDate tsd WHERE tsd.tour.id = :tourId ORDER BY tsd.startDate")
    List<LocalDate> findStartDatesByTourId(@Param("tourId") Long tourId);

    @Query("SELECT tsd FROM TourStartDate tsd WHERE tsd.tour.id = :tourId AND tsd.startDate = :date")
    Optional<TourStartDate> findByTourIdAndStartDate(@Param("tourId") Long tourId, @Param("date") LocalDate date);
}