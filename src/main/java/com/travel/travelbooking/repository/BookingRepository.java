package com.travel.travelbooking.repository;

import com.travel.travelbooking.dto.BookingStatsDTO;
import com.travel.travelbooking.dto.LatestBookingDTO;
import com.travel.travelbooking.entity.Booking;
import com.travel.travelbooking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    /**
     * Kiểm tra user đã từng đặt tour này và booking đã COMPLETED chưa
     */
    boolean existsByUserIdAndTourIdAndStatus(
            Long userId,
            Long tourId,
            BookingStatus status
    );

    /**
     * (Tùy chọn) Lấy booking COMPLETED của user với tour
     */
    default Booking findCompletedBookingByUserAndTour(Long userId, Long tourId) {
        return findByUserIdAndTourIdAndStatus(userId, tourId, BookingStatus.COMPLETED)
                .stream().findFirst().orElse(null);
    }

    List<Booking> findByUserIdAndTourIdAndStatus(
            Long userId,
            Long tourId,
            BookingStatus status
    );

    /**
     * LẤY 5 ĐƠN ĐẶT TOUR GẦN NHẤT (CONFIRMED, PENDING, CANCELLED...)
     */
    @Query("""
    SELECT new com.travel.travelbooking.dto.LatestBookingDTO(
    CONCAT('DH', LPAD(CAST(b.id AS string), 6, '0')),
    c.fullName,
    c.phoneNumber,
    COALESCE(u.avatarUrl, '/default-avatar.jpg'),
    t.name,
    b.bookingDate,
    b.status,
    b.totalPrice
)
    FROM Booking b
    LEFT JOIN b.tour t
    LEFT JOIN b.contact c
    LEFT JOIN b.user u
    WHERE b.status <> 'DELETED'
    ORDER BY b.bookingDate DESC
    """)
    Page<LatestBookingDTO> findLatestBookingsDTO(Pageable pageable);


    default List<LatestBookingDTO> findTop5LatestBookings() {
        return findLatestBookingsDTO(PageRequest.of(0, 5)).getContent();
    }

    // Doanh thu thực tế (chỉ tính đơn đã hoàn thành tour)
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'COMPLETED'")
    Double getActualRevenue();

    // Doanh thu dự kiến (đã xác nhận + đã hoàn thành = chắc chắn thu được)
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status IN ('CONFIRMED', 'COMPLETED')")
    Double getExpectedRevenue();
}
