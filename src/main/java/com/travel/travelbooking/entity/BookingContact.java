package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_contacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    // Có thể là user đã đăng nhập, hoặc null nếu khách vãng lai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}