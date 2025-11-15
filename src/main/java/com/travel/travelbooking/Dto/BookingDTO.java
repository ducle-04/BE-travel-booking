package com.travel.travelbooking.Dto;

import com.travel.travelbooking.Entity.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;
    private Long tourId;
    private String tourName;
    private String destinationName;
    private LocalDateTime startDate;
    private int numberOfPeople;
    private Double totalPrice;
    private LocalDateTime bookingDate;
    private BookingStatus status;
    private String note;
    private String cancelReason;
    private Long userId;
    private String userFullname;
    private String userPhone;
    private String userAvatarUrl;
}
