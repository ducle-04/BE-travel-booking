package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.BookingDTO;
import com.travel.travelbooking.dto.PaymentDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.PaymentRepository;
import com.travel.travelbooking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 1. Admin/Staff cập nhật trạng thái thanh toán thủ công
    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<?> updatePaymentStatus(
            @PathVariable Long bookingId,
            @RequestParam PaymentStatus status) {

        BookingDTO dto = paymentService.updatePaymentStatus(bookingId, status);

        return new ApiResponse<>("Cập nhật thành công", dto);
    }

    // 2. Tạo payment MoMo cho booking (user đã có booking trước)
    @PostMapping("/momo/create/{bookingId}")
    @PreAuthorize("isAuthenticated()") // hoặc permitAll nếu cho khách vãng lai
    public ApiResponse<?> createMomoPayment(@PathVariable Long bookingId) throws Exception {
        String payUrl = paymentService.createMomoPaymentUrl(bookingId);
        return new ApiResponse<>("Tạo thanh toán MoMo thành công", payUrl);
    }

    // 3. IPN callback từ MoMo (MoMo gọi vào, phải permitAll)
    @PostMapping("/momo/ipn")
    public ApiResponse<?> momoIpn(@RequestBody Map<String, Object> body) throws Exception {
        // Lấy các field cần thiết
        String orderId = (String) body.get("orderId");
        int resultCode = (int) body.get("resultCode");
        String signature = (String) body.get("signature");

        // OPTIONAL: nếu muốn verify chữ ký, build rawData lại từ map body

        PaymentDTO dto = paymentService.handleMomoIpn(orderId, resultCode, signature, null);

        return new ApiResponse<>("IPN nhận thành công", dto);
    }
}
