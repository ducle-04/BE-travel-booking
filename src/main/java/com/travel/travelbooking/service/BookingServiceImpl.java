package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.BookingCreateRequest;
import com.travel.travelbooking.dto.BookingDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.TourRepository;
import com.travel.travelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourService tourService;

    @Override
    public BookingDTO createBooking(BookingCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));

        if (tour.getStatus() != TourStatus.ACTIVE) {
            throw new IllegalArgumentException("Tour không khả dụng để đặt");
        }

        long currentParticipants = bookingRepository.getCurrentParticipants(tour.getId());
        long requested = currentParticipants + request.getNumberOfPeople();
        if (requested > tour.getMaxParticipants()) {
            throw new IllegalArgumentException(
                    "Chỉ còn " + (tour.getMaxParticipants() - currentParticipants) + " chỗ trống"
            );
        }

        double totalPrice = tour.getPrice() * request.getNumberOfPeople();

        Booking booking = Booking.builder()
                .user(user)
                .tour(tour)
                .numberOfPeople(request.getNumberOfPeople())
                .totalPrice(totalPrice)
                .startDate(request.getStartDate().atStartOfDay())
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .build();

        return toDTO(bookingRepository.save(booking)); // toDTO RIÊNG TRONG IMPL
    }

    @Override
    public BookingDTO requestCancel(Long bookingId, String reason, Long userId) {
        Booking booking = getBookingByIdAndUser(bookingId, userId);

        if (booking.getStatus() != BookingStatus.PENDING &&
                booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ được hủy booking khi đang chờ xác nhận hoặc đã xác nhận");
        }

        booking.setStatus(BookingStatus.CANCEL_REQUEST);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setNote("Yêu cầu hủy: " + reason);
        }
        return toDTO(bookingRepository.save(booking));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public BookingDTO confirmBooking(Long bookingId) {
        Booking booking = getBookingForAdmin(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ xác nhận được booking đang chờ");
        }

        long current = bookingRepository.getCurrentParticipants(booking.getTour().getId());
        long after = current + booking.getNumberOfPeople();
        if (after > booking.getTour().getMaxParticipants()) {
            throw new IllegalArgumentException("Không đủ chỗ trống để xác nhận");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        updateTourParticipants(booking.getTour().getId());
        return toDTO(bookingRepository.save(booking));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public BookingDTO rejectBooking(Long bookingId, String reason) {
        Booking booking = getBookingForAdmin(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ từ chối được booking đang chờ");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setNote("Từ chối: " + (reason != null ? reason : "Không đủ điều kiện"));
        return toDTO(bookingRepository.save(booking));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public BookingDTO approveCancellation(Long bookingId) {
        Booking booking = getBookingForAdmin(bookingId);

        if (booking.getStatus() != BookingStatus.CANCEL_REQUEST) {
            throw new IllegalArgumentException("Không có yêu cầu hủy");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        updateTourParticipants(booking.getTour().getId());
        return toDTO(bookingRepository.save(booking));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public BookingDTO rejectCancellation(Long bookingId, String reason) {
        Booking booking = getBookingForAdmin(bookingId);

        if (booking.getStatus() != BookingStatus.CANCEL_REQUEST) {
            throw new IllegalArgumentException("Không có yêu cầu hủy");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setNote("Yêu cầu hủy bị từ chối: " + (reason != null ? reason : "Không hợp lệ"));
        return toDTO(bookingRepository.save(booking));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public void softDeleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại"));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Không thể xóa booking đã xác nhận. Vui lòng hủy trước.");
        }

        booking.setStatus(BookingStatus.DELETED);
        bookingRepository.save(booking);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public BookingDTO completeBooking(Long bookingId) {
        Booking booking = getBookingForAdmin(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể hoàn thành booking đã xác nhận");
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartDate() != null && booking.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException("Tour chưa diễn ra, không thể hoàn thành");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return toDTO(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> getMyBookings(Long userId, int page, List<BookingStatus> statuses) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("bookingDate").descending());

        Page<Booking> bookings;
        if (statuses == null || statuses.isEmpty()) {
            bookings = bookingRepository.findByUserIdAndStatusNot(userId, BookingStatus.DELETED, pageable);
        } else {
            bookings = bookingRepository.findByUserIdAndStatusInAndStatusNot(
                    userId, statuses, BookingStatus.DELETED, pageable
            );
        }
        return bookings.map(this::toDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Transactional(readOnly = true)
    public Page<BookingDTO> getPendingBookings(int page, List<BookingStatus> statuses) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("bookingDate").ascending());

        if (statuses == null || statuses.isEmpty()) {
            return bookingRepository.findByStatusNot(BookingStatus.DELETED, pageable)
                    .map(this::toDTO);
        }

        return bookingRepository.findByStatusInAndStatusNot(
                statuses, BookingStatus.DELETED, pageable
        ).map(this::toDTO);
    }

    // === GIỮ NGUYÊN LOGIC CŨ – toDTO RIÊNG TRONG IMPL ===
    private Booking getBookingByIdAndUser(Long id, Long userId) {
        return bookingRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking của bạn"));
    }

    private Booking getBookingForAdmin(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại"));
    }

    private void updateTourParticipants(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));
        long count = bookingRepository.getCurrentParticipants(tourId);
        tour.setTotalParticipants((int) count);
        tourRepository.save(tour);
    }

    private BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setTourId(b.getTour().getId());
        dto.setTourName(b.getTour().getName());
        dto.setDestinationName(b.getTour().getDestination().getName());
        dto.setStartDate(b.getStartDate());
        dto.setNumberOfPeople(b.getNumberOfPeople());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setBookingDate(b.getBookingDate());
        dto.setStatus(b.getStatus());
        dto.setNote(b.getNote());
        dto.setUserId(b.getUser().getId());
        dto.setUserFullname(b.getUser().getFullname());
        dto.setUserPhone(b.getUser().getPhoneNumber());
        dto.setUserAvatarUrl(b.getUser().getAvatarUrl());
        return dto;
    }
}