package com.travel.travelbooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlogSummaryDTO {
    private Long id;
    private String title;
    private String thumbnail;
    private String shortDescription; // Mô tả ngắn
    private String authorName;
    private LocalDateTime createdAt;
    private int views;
    private int commentCount; // Số lượng bình luận đã duyệt
}