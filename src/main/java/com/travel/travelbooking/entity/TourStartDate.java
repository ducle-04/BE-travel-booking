package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "tour_start_dates")
public class TourStartDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false)
    private LocalDate startDate;

    // Số chỗ cho NGÀY khởi hành này
    @Column(nullable = false)
    private Integer capacity;

    // Số khách đã đặt NGÀY này
    @Column(nullable = false)
    private Integer bookedParticipants = 0;

    public TourStartDate() {}

    public TourStartDate(Tour tour, LocalDate startDate) {
        this.tour = tour;
        this.startDate = startDate;
        this.capacity = tour.getMaxParticipants(); // default = maxParticipants của tour
        this.bookedParticipants = 0;
    }
}
