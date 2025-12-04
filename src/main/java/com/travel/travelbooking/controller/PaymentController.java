package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.PaymentDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<?> updatePaymentStatus(
            @PathVariable Long bookingId,
            @RequestParam PaymentStatus status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        Payment payment = booking.getPayment();
        if (payment == null) {
            throw new RuntimeException("Booking chưa có thông tin thanh toán");
        }

        payment.setStatus(status);

        if (status == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);

        PaymentDTO dto = new PaymentDTO(
                payment.getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getPaidAt(),
                booking.getId()
        );

        return new ApiResponse<>("Cập nhật trạng thái thanh toán thành công", dto);
    }

}
