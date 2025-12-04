package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.PaymentMethod;
import com.travel.travelbooking.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long id;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    private Long bookingId;
}

