package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.PaymentDTO;

public interface PaymentService {

    // Tạo link thanh toán MoMo cho booking
    String createMomoPaymentUrl(Long bookingId) throws Exception;

    // Xử lý IPN callback từ MoMo
    PaymentDTO handleMomoIpn(String orderId, int resultCode, String signature, String rawData) throws Exception;
}
