package com.travel.travelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LatestTourDTO {
    private Long tourId;
    private String tourName;
    private String imageUrl;
    private String destinationName;
    private LocalDateTime createdAt;
}