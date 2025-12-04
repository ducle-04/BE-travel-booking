package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;
    private Long tourId;
    private String tourName;
    private String destinationName;

    private LocalDate selectedStartDate;
    private String selectedTransportName;
    private Double selectedTransportPrice;

    private int numberOfPeople;
    private Double totalPrice;
    private LocalDateTime bookingDate;
    private BookingStatus status;
    private String note;

    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private boolean guest;

    private Long userId;
    private String userFullname;
    private String userPhone;
    private String userAvatarUrl;
}