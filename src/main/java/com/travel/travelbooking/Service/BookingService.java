package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.BookingCreateRequest;
import com.travel.travelbooking.Dto.BookingDTO;
import com.travel.travelbooking.Entity.BookingStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {

    BookingDTO createBooking(BookingCreateRequest request, Long userId);
    BookingDTO requestCancel(Long bookingId, String reason, Long userId);
    Page<BookingDTO> getMyBookings(Long userId, int page, List<BookingStatus> statuses);

    BookingDTO confirmBooking(Long bookingId);
    BookingDTO rejectBooking(Long bookingId, String reason);
    BookingDTO approveCancellation(Long bookingId);
    BookingDTO rejectCancellation(Long bookingId, String reason);
    void softDeleteBooking(Long bookingId);
    BookingDTO completeBooking(Long bookingId);
    Page<BookingDTO> getPendingBookings(int page, List<BookingStatus> statuses);
}