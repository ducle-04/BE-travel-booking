package com.travel.travelbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    private String subject;  // Chỉ required cho initial

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000, message = "Nội dung không vượt quá 2000 ký tự")
    private String content;

    // Cho guest
    private String guestName;
    private String guestEmail;
    private String guestPhone;
}