package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.BookingCreateRequest;
import com.travel.travelbooking.dto.BookingDTO;
import com.travel.travelbooking.dto.BookingStatsDTO;
import com.travel.travelbooking.entity.BookingStatus;
import com.travel.travelbooking.entity.User;
import com.travel.travelbooking.entity.UserStatus;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.BookingService;
import com.travel.travelbooking.service.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserServiceImpl userServiceImpl;

    public BookingController(BookingService bookingService, UserServiceImpl userServiceImpl) {
        this.bookingService = bookingService;
        this.userServiceImpl = userServiceImpl;
    }

    private User validateUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            throw new IllegalArgumentException("Người dùng chưa đăng nhập");

        User user = userServiceImpl.findByUsername(userDetails.getUsername());
        if (user == null || user.getStatus() == null || "DELETED".equals(user.getStatus().name()))
            throw new IllegalArgumentException("Tài khoản không hợp lệ hoặc đã bị xóa");

        return user;
    }

    // 1. Khách hàng đặt tour mới
    @PostMapping
    public ResponseEntity<?> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingCreateRequest request) {

        Long userId = null;
        if (userDetails != null) {
            User user = userServiceImpl.findByUsername(userDetails.getUsername());
            if (user != null && user.getStatus() != UserStatus.DELETED) {
                userId = user.getId();
            }
        }

        BookingDTO dto = bookingService.createBooking(request, userId);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Đặt tour thành công! Chúng tôi sẽ liên hệ xác nhận sớm", dto));
    }

    // 2. Khách hàng yêu cầu hủy booking
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> requestCancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) String reason) {
        User user = validateUser(userDetails);
        BookingDTO booking = bookingService.requestCancel(id, reason, user.getId());
        return ResponseEntity.ok(new ApiResponse<>("Đã gửi yêu cầu hủy tour", booking));
    }

    // 3. Khách hàng xem lịch sử booking + lọc trạng thái
    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<BookingStatus> status) {  // Thêm filter
        User user = validateUser(userDetails);
        Page<BookingDTO> bookings = bookingService.getMyBookings(user.getId(), page, status);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách booking thành công", bookings));
    }

    // 4. Admin/Staff xem danh sách cần xử lý + lọc trạng thái
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<BookingStatus> status) {  // Thêm filter
        Page<BookingDTO> bookings = bookingService.getPendingBookings(page, status);
        return ResponseEntity.ok(new ApiResponse<>("Danh sách booking cần xử lý", bookings));
    }

    // 5. Admin/Staff xác nhận booking
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(new ApiResponse<>("Xác nhận booking thành công", booking));
    }

    // 6. Admin/Staff từ chối booking
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id, @RequestBody(required = false) String reason) {
        BookingDTO booking = bookingService.rejectBooking(id, reason);
        return ResponseEntity.ok(new ApiResponse<>("Từ chối booking thành công", booking));
    }

    // 7. Admin/Staff đồng ý hủy booking
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @PatchMapping("/{id}/cancel/approve")
    public ResponseEntity<?> approveCancellation(@PathVariable Long id) {
        BookingDTO booking = bookingService.approveCancellation(id);
        return ResponseEntity.ok(new ApiResponse<>("Đồng ý hủy tour thành công", booking));
    }

    // 8. Admin/Staff từ chối yêu cầu hủy
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @PatchMapping("/{id}/cancel/reject")
    public ResponseEntity<?> rejectCancellation(@PathVariable Long id, @RequestBody(required = false) String reason) {
        BookingDTO booking = bookingService.rejectCancellation(id, reason);
        return ResponseEntity.ok(new ApiResponse<>("Từ chối yêu cầu hủy thành công", booking));
    }

    // 9. Admin/Staff xóa mềm booking
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteBooking(@PathVariable Long id) {
        bookingService.softDeleteBooking(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa booking thành công", null));
    }

    // 10. Admin/Staff đánh dấu hoàn thành tour
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.completeBooking(id);
        return ResponseEntity.ok(new ApiResponse<>("Tour đã được đánh dấu hoàn thành", booking));
    }

    // 11. Admin/Staff xem chi tiết 1 booking bất kỳ
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingDetail(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingDetailById(id);
        return ResponseEntity.ok(new ApiResponse<>("Chi tiết booking", booking));
    }

    // 12. Dashboard thống kê trạng thái booking
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @GetMapping("/stats")
    public ResponseEntity<?> getBookingStats() {
        BookingStatsDTO stats = bookingService.getBookingStatistics();
        return ResponseEntity.ok(new ApiResponse<>("Thống kê booking", stats));
    }
}