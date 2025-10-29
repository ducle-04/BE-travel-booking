package com.travel.travelbooking.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "tour_details")
public class TourDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false, unique = true)
    private Tour tour;

    @Column(nullable = false, length = 500)
    private String transportation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String itinerary;

    @Column(nullable = false, length = 300)
    private String departurePoint;

    @Column(nullable = false, length = 100)
    private String departureTime;

    @Column(nullable = false, length = 500)
    private String suitableFor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String cancellationPolicy;

    @ElementCollection
    @CollectionTable(name = "tour_additional_images", joinColumns = @JoinColumn(name = "tour_detail_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> additionalImages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_videos", joinColumns = @JoinColumn(name = "tour_detail_id"))
    @Column(name = "video_url", length = 1000)
    private List<String> videos = new ArrayList<>();
}