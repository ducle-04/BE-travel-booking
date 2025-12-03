package com.travel.travelbooking.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HotelDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String starRating;
    private List<String> images = new ArrayList<>();
    private List<String> videos = new ArrayList<>();
}