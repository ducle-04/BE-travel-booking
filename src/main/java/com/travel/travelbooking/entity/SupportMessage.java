package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "support_messages")
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(optional = true)
    @JoinColumn(name = "sender_id")
    private User sender;  // Null nếu từ guest, không null nếu từ staff/user

    private boolean fromGuest;

    private boolean isRead = false;  // ĐÃ SỬA: read → isRead (tránh lỗi MySQL)

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}