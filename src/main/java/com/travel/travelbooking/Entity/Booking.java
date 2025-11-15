package com.travel.travelbooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false)
    private int numberOfPeople;

    @Column(nullable = false)
    private Double totalPrice;

    private LocalDateTime startDate;

    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(
            columnDefinition = "ENUM('PENDING','CONFIRMED','CANCEL_REQUEST','CANCELLED','REJECTED','COMPLETED','DELETED')",
            nullable = false
    )
    private BookingStatus status;


    private String note;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }
}
