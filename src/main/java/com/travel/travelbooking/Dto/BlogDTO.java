package com.travel.travelbooking.Dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogDTO {
    private Long id;
    private String title;
    private String content; // Nội dung đầy đủ
    private String thumbnail;
    private String authorName;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int views;
    private List<String> images; // Danh sách ảnh chi tiết từ BlogImage
}