package com.travel.travelbooking.Entity;

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
    private String duration; // Ví dụ: "3 ngày 2 đêm"

    @Column(nullable = false)
    private Double price;

    private String imageUrl;

    @Column(length = 3000)
    private String description;

    private Double averageRating;

    private Integer totalParticipants = 0;

    // 🟢 Trạng thái tour
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TourStatus status;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        averageRating = 0.0;
        status = TourStatus.ACTIVE; // mặc định hoạt động
    }
}
