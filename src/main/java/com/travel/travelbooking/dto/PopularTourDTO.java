package com.travel.travelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor      // ← BẮT BUỘC PHẢI CÓ
@AllArgsConstructor
public class PopularTourDTO {
    private Long tourId;
    private String tourName;
    private String imageUrl;
    private String destinationName;
    private Long views;
    private Long bookingsCount;
    private Long reviewsCount;
    private double averageRating;
    private double popularityScore;
}