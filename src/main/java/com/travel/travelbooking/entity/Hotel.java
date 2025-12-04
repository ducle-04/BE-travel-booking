package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String address; // VD: "123 Nguyễn Huệ, Quận 1, TP.HCM"

    @Column(nullable = false)
    private Integer starRating; // 1-5

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotelStatus status = HotelStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "hotel_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "hotel_videos", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "video_url", length = 1000)
    private List<String> videos = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum HotelStatus {
        ACTIVE, INACTIVE
    }
}