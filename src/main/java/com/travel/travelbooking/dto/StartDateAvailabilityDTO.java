package com.travel.travelbooking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StartDateAvailabilityDTO {
    private LocalDate date;
    private String formattedDate;     // Ví dụ: "15/12/2025 (T2)"
    private int remainingSeats;       // Số chỗ còn lại (chung cho cả tour)
    private boolean available;        // true nếu còn chỗ
}