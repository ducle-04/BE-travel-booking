package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.ConversationStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationDTO {
    private Long id;
    private Long userId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String subject;
    private ConversationStatus status;
    private LocalDateTime createdAt;
    private List<SupportMessageDTO> messages;
}