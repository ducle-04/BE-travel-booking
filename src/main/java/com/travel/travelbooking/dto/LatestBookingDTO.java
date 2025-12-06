package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestBookingDTO {

    private String bookingId;          // CONCAT('DH', LPAD(...))
    private String customerName;       // c.fullName
    private String customerPhone;      // c.phoneNumber
    private String customerAvatarUrl;  // COALESCE(u.avatarUrl, ...)
    private String tourName;           // t.name
    private LocalDateTime bookingDate; // b.bookingDate
    private BookingStatus status;      // b.status
    private Double totalPrice;         // b.totalPrice
}
