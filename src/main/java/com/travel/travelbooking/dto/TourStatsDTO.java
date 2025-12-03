package com.travel.travelbooking.dto;

import lombok.Data;

@Data
public class TourStatsDTO {
    private Long totalTours;
    private Long activeTours;
    private Long inactiveTours;
    private Long totalBookings;

    public TourStatsDTO(Long totalTours, Long activeTours, Long inactiveTours, Long totalBookings) {
        this.totalTours = totalTours;
        this.activeTours = activeTours;
        this.inactiveTours = inactiveTours;
        this.totalBookings = totalBookings;
    }
}