package com.travel.travelbooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopBookedTourDTO {
    private Long tourId;
    private String tourName;
    private String imageUrl;
    private String destinationName;
    private Long bookingCount;
}