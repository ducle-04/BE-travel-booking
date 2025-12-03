package com.travel.travelbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentDTO {
    
    // Luồng rẽ nhánh R4: Nội dung không hợp lệ
    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 1, max = 2000, message = "Bình luận phải dưới 2000 ký tự")
    private String content;
}