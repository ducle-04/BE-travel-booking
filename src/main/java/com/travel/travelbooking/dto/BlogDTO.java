package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.BlogStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BlogDTO {

    private Long id;
    private String title;
    private String content;
    private String thumbnail;
    private String authorName;
    private Long authorId;

    private BlogStatus status;

    private List<String> images = new ArrayList<>();
    private Integer views;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
