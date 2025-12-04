package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Booking liên quan
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;  // DIRECT hoặc ONLINE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionId; // Mã giao dịch (online)

    private LocalDateTime paidAt; // Thời điểm thanh toán

    @PrePersist
    protected void onCreate() {
        if (status == null)
            status = PaymentStatus.PENDING;
    }
}
