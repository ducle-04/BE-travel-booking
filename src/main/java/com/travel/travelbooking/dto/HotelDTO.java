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
    private Integer starRating;
    private String status;
    private List<String> images = new ArrayList<>();
    private List<String> videos = new ArrayList<>();


}