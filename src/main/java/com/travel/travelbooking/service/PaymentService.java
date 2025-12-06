package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.BookingDTO;
import com.travel.travelbooking.dto.PaymentDTO;
import com.travel.travelbooking.entity.PaymentStatus;

public interface PaymentService {

    BookingDTO updatePaymentStatus(Long bookingId, PaymentStatus status);

    // Tạo link thanh toán MoMo cho booking
    String createMomoPaymentUrl(Long bookingId) throws Exception;

    // Xử lý IPN callback từ MoMo
    PaymentDTO handleMomoIpn(String orderId, int resultCode, String signature, String rawData) throws Exception;
}
