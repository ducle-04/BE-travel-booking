package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // nullable → cho phép khách vãng lai
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    // NGÀY KHỞI HÀNH MÀ KHÁCH CHỌN
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_date_id", nullable = false)
    private TourStartDate selectedStartDate;

    // PHƯƠNG TIỆN KHÁCH CHỌN (giữ @Embeddable nên chỉ lưu tên + giá)
    @Column(length = 100)
    private String selectedTransportName;

    @Column(nullable = false)
    private Double selectedTransportPrice = 0.0;

    @Column(nullable = false)
    private int numberOfPeople;

    @Column(nullable = false)
    private Double totalPrice; // = (tour.price + transportPrice) × numberOfPeople

    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING','CONFIRMED','CANCEL_REQUEST','CANCELLED','REJECTED','COMPLETED','DELETED')",
            nullable = false)
    private BookingStatus status;

    private String note;

    // THÔNG TIN LIÊN HỆ NGƯỜI ĐẶT (bắt buộc)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "contact_id", nullable = false)
    private BookingContact contact;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (status == null) status = BookingStatus.PENDING;
    }
}