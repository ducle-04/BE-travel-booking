package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsDTO {
    private long totalBookings;

    private long pending;
    private long confirmed;
    private long cancelRequest;
    private long cancelled;
    private long rejected;
    private long completed;

}