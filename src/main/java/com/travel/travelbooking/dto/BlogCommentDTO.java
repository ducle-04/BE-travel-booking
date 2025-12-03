package com.travel.travelbooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlogCommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String username;
    private String userAvatarUrl;
}