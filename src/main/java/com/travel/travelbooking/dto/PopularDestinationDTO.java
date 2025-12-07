package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.Region;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularDestinationDTO {
    private Long destinationId;
    private String destinationName;
    private String imageUrl;
    private Region region;
    private String description;
    private Long tourCount;
    private Long totalViews;
    private Long bookingCount;
}