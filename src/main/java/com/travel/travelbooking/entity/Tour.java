package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "tours")
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private Double price;

    private String imageUrl;

    @Column(length = 3000)
    private String description;

    private Double averageRating;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(name = "views", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long views = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TourStatus status;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TourCategory category;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourStartDate> startDates;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TourDetail detail;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        averageRating = 0.0;
        status = TourStatus.ACTIVE;
        if (maxParticipants == null || maxParticipants <= 0) {
            maxParticipants = 50; // Mặc định 50 nếu không nhập
        }
    }
}