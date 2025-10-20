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

    // Người đặt tour
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Tour được đặt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    // Số lượng người đi
    @Column(nullable = false)
    private int numberOfPeople;

    // Tổng tiền
    @Column(nullable = false)
    private Double totalPrice;

    // Ngày khởi hành
    private LocalDateTime startDate;

    // Ngày đặt
    private LocalDateTime bookingDate;

    // Trạng thái đặt tour
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String note;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (status == null) status = BookingStatus.PENDING;
    }
}
