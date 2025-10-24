package com.travel.travelbooking.Dto;

import com.travel.travelbooking.Entity.TourStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TourDTO {
    private Long id;
    private String name;
    private Long destinationId; // Giữ lại nhưng không bắt buộc
    private String destinationName; // Thêm trường này để nhận tên điểm đến
    private String duration;
    private Double price;
    private String imageUrl;
    private String description;
    private Double averageRating;
    private Integer totalParticipants;
    private TourStatus status;
    private LocalDateTime createdAt;
    private Long bookingsCount;
    private Long reviewsCount;

    public TourDTO() {}

    // Constructor cơ bản
    public TourDTO(Long id, String name, Long destinationId, String destinationName, String duration,
                   Double price, String imageUrl, String description, Double averageRating,
                   Integer totalParticipants, TourStatus status, LocalDateTime createdAt) {
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
    }

    // Constructor cho query có COUNT bookings và reviews
    public TourDTO(Long id, String name, Long destinationId, String destinationName, String duration,
                   Double price, String imageUrl, String description, Double averageRating,
                   Integer totalParticipants, TourStatus status, LocalDateTime createdAt,
                   Long bookingsCount, Long reviewsCount) {
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
    }
}