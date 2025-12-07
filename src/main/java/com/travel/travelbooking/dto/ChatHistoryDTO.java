package com.travel.travelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryDTO {
    private Long id;
    private String userMessage;
    private String botReply;
    private LocalDateTime timestamp;
    private String username;
}