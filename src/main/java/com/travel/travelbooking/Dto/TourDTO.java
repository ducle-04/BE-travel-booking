package com.travel.travelbooking.Dto;

import com.travel.travelbooking.Entity.TourStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TourDTO {
    private Long id;
    private String name;
    private Long destinationId;
    private String destinationName;
    private String duration;
    private Double price;
    private String imageUrl;
    private String description;
    private Double averageRating;
    private Integer totalParticipants;
    private Integer maxParticipants;
    private TourStatus status;
    private LocalDateTime createdAt;
    private Long bookingsCount;
    private Long reviewsCount;

    public TourDTO() {}

    // Constructor cơ bản (không có COUNT)
    public TourDTO(Long id, String name, Long destinationId, String destinationName, String duration,
                   Double price, String imageUrl, String description, Double averageRating,
                   Integer totalParticipants, TourStatus status, LocalDateTime createdAt, Integer maxParticipants) {
        this.id = id;
        this.name = name;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.duration = duration;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.averageRating = averageRating;
        this.totalParticipants = totalParticipants;
        this.status = status;
        this.createdAt = createdAt;
        this.maxParticipants = maxParticipants;
    }

    // Constructor có COUNT (bookings, reviews)
    public TourDTO(Long id, String name, Long destinationId, String destinationName, String duration,
                   Double price, String imageUrl, String description, Double averageRating,
                   Integer totalParticipants, TourStatus status, LocalDateTime createdAt,
                   Long bookingsCount, Long reviewsCount, Integer maxParticipants) {
        this.id = id;
        this.name = name;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.duration = duration;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.averageRating = averageRating;
        this.totalParticipants = totalParticipants;
        this.status = status;
        this.createdAt = createdAt;
        this.bookingsCount = bookingsCount;
        this.reviewsCount = reviewsCount;
        this.maxParticipants = maxParticipants;
    }
}