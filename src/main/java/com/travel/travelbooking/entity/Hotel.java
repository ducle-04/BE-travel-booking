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

    @Column(length = 300)
    private String address;

    @Column(length = 10)
    private String starRating; // "3 sao", "4 sao", "5 sao"

    @ElementCollection
    @CollectionTable(name = "hotel_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "hotel_videos", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "video_url", length = 1000)
    private List<String> videos = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}