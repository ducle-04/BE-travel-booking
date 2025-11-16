package com.travel.travelbooking.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogCreateDTO {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không quá 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    
    // Ảnh thumbnail và ảnh nội dung sẽ được truyền dưới dạng file riêng biệt trong controller
}
