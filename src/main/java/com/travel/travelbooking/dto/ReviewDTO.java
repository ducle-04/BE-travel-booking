package com.travel.travelbooking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userFullname;
    private String userAvatarUrl;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long tourId;
    private String tourName;
}