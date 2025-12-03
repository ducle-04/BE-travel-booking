package com.travel.travelbooking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingCreateRequest {
    private Long tourId;
    private int numberOfPeople;
    private LocalDate startDate;
    private String note;
}
