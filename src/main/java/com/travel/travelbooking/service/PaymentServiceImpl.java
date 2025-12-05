package com.travel.travelbooking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.config.MomoConfig;
import com.travel.travelbooking.dto.PaymentDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.PaymentRepository;
import com.travel.travelbooking.util.MomoSignatureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String createMomoPaymentUrl(Long bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
            throw new RuntimeException("Tổng tiền không hợp lệ");
        }

        Payment payment = booking.getPayment();
        if (payment == null) {
            // Nếu chưa có payment thì tạo
            payment = Payment.builder()
                    .booking(booking)
                    .method(PaymentMethod.MOMO)
                    .status(PaymentStatus.PENDING)
                    .build();
        }

        long amount = booking.getTotalPrice().longValue();

        String orderId = bookingId + "-" + System.currentTimeMillis();
        String requestId = String.valueOf(System.currentTimeMillis());

        // Lưu lại orderId vào transactionId để map ngược khi IPN
        payment.setTransactionId(orderId);
        paymentRepository.save(payment);

        String requestType = "captureWallet";
        String extraData = ""; // có thể đính kèm info khác nếu muốn

        // raw hash theo quy tắc của MoMo
        String rawHash = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + momoConfig.getIpnUrl()
                + "&orderId=" + orderId
                + "&orderInfo=" + "Thanh toán booking " + bookingId
                + "&partnerCode=" + momoConfig.getPartnerCode()
                + "&redirectUrl=" + momoConfig.getRedirectUrl() + "?bookingId=" + bookingId
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = MomoSignatureUtil.hmacSHA256(rawHash, momoConfig.getSecretKey());

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", momoConfig.getPartnerCode());
        body.put("partnerName", "MoMo");
        body.put("storeId", "WonderTrail");
        body.put("requestId", requestId);
        body.put("amount", String.valueOf(amount));
        body.put("orderId", orderId);
        body.put("orderInfo", "Thanh toán booking " + bookingId);
        body.put("redirectUrl", momoConfig.getRedirectUrl() + "?bookingId=" + bookingId);
        body.put("ipnUrl", momoConfig.getIpnUrl());
        body.put("lang", "vi");
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(momoConfig.getEndpoint(), request, String.class);
        JsonNode json = objectMapper.readTree(response);

        if (!json.has("payUrl")) {
            throw new RuntimeException("Không lấy được payUrl từ MoMo: " + response);
        }

        return json.get("payUrl").asText();
    }

    @Override
    public PaymentDTO handleMomoIpn(String orderId,
                                    int resultCode,
                                    String signature,
                                    String rawData) throws Exception {

        // TODO: bạn có thể verify lại signature bằng rawData nếu muốn chắc chắn tuyệt đối

        Payment payment = paymentRepository.findByTransactionId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với orderId: " + orderId));

        if (resultCode == 0) {
            payment.setStatus(PaymentStatus.PAID);
            if (payment.getPaidAt() == null) {
                payment.setPaidAt(LocalDateTime.now());
            }
        } else if (resultCode == 9000) { // ví dụ code hủy
            payment.setStatus(PaymentStatus.CANCELLED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        Booking booking = payment.getBooking();

        return new PaymentDTO(
                payment.getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getPaidAt(),
                booking.getId()
        );
    }
}
