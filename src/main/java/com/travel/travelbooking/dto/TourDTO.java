package com.travel.travelbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.travel.travelbooking.entity.TourStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TourDTO {
    private Long id;

    @NotBlank(message = "Tên tour không được để trống")
    @Size(max = 150, message = "Tên tour không được vượt quá 150 ký tự")
    private String name;

    private Long destinationId;

    @NotBlank(message = "Tên điểm đến không được để trống")
    @Size(max = 100, message = "Tên điểm đến không được vượt quá 100 ký tự")
    private String destinationName;

    @NotBlank(message = "Thời gian tour không được để trống")
    @Pattern(regexp = "^\\d+ (ngày|đêm|ngày đêm)$", message = "Thời gian phải có định dạng: 'số ngày', 'số đêm' hoặc 'số ngày đêm'")
    private String duration;

    @NotNull(message = "Giá tour không được để trống")
    @Positive(message = "Giá tour phải lớn hơn 0")
    @Digits(integer = 10, fraction = 2, message = "Giá tour không hợp lệ")
    private Double price;

    private String imageUrl;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    private Double averageRating;
    private Integer totalParticipants;

    @NotNull(message = "Số người tối đa không được để trống")
    @Min(value = 1, message = "Số người tối đa phải ít nhất là 1")
    private Integer maxParticipants;
    private Long views;
    private TourStatus status;
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private List<LocalDate> startDates = new ArrayList<>();

    // Chỉ có trong response
    private Long bookingsCount;
    private Long reviewsCount;
    private TourDetailDTO tourDetail;

    // MỚI: Loại tour
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;

    public TourDTO() {}

    // Constructor cũ (không count)
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

    // Constructor có count + category
    public TourDTO(Long id, String name, Long destinationId, String destinationName, String duration,
                   Double price, String imageUrl, String description, Double averageRating,
                   Integer totalParticipants, TourStatus status, LocalDateTime createdAt,
                   Long bookingsCount, Long reviewsCount, Integer maxParticipants,
                   Long categoryId, String categoryName, String categoryIcon, Long views) {
        this(id, name, destinationId, destinationName, duration, price, imageUrl, description,
                averageRating, totalParticipants, status, createdAt, maxParticipants);
        this.bookingsCount = bookingsCount;
        this.reviewsCount = reviewsCount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.views = views != null ? views : 0L;
    }
}