package com.travel.travelbooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupportMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private boolean fromGuest;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
}