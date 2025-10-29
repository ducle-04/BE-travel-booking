// src/main/java/com/travel/travelbooking/Dto/TourDetailDTO.java
package com.travel.travelbooking.Dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TourDetailDTO {
    private String transportation;
    private String itinerary;
    private String departurePoint;
    private String departureTime;
    private String suitableFor;
    private String cancellationPolicy;
    private List<String> additionalImages = new ArrayList<>();
    private List<String> videos = new ArrayList<>();
}