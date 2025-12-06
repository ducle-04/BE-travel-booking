package com.travel.travelbooking.repository;

import com.travel.travelbooking.dto.BookingStatsDTO;
import com.travel.travelbooking.entity.Booking;
import com.travel.travelbooking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserIdAndStatusNot(Long userId, BookingStatus status, Pageable pageable);
    // Thêm phương thức tìm theo userId + status (không tính DELETED)
    Page<Booking> findByUserIdAndStatusInAndStatusNot(
            Long userId,
            List<BookingStatus> statuses,
            BookingStatus excludedStatus,
            Pageable pageable
    );

    Page<Booking> findByStatusNot(BookingStatus status, Pageable pageable);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    Page<Booking> findByStatusIn(List<BookingStatus> statuses, Pageable pageable);

    Page<Booking> findByStatusInAndStatusNot(List<BookingStatus> statuses, BookingStatus excludedStatus, Pageable pageable);

    long countByTourIdAndStatus(Long tourId, BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM Booking b WHERE b.tour.id = :tourId AND b.status = 'CONFIRMED'")
    long getCurrentParticipants(@Param("tourId") Long tourId);

    List<Booking> findByContactEmailAndUserIsNull(String email);

    @Query("""
    SELECT new com.travel.travelbooking.dto.BookingStatsDTO(
        COUNT(b),
        SUM(CASE WHEN b.status = 'PENDING' THEN 1 ELSE 0 END),
        SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN b.status = 'CANCEL_REQUEST' THEN 1 ELSE 0 END),
        SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN b.status = 'REJECTED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END)
    )
    FROM Booking b
    WHERE b.status != 'DELETED'
    """)
    BookingStatsDTO getBookingStatistics();
}
