package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.DestinationStatus;
import com.travel.travelbooking.entity.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DestinationDTO {
    private Long id;

    @NotBlank(message = "Tên điểm đến không được để trống")
    @Size(max = 100, message = "Tên điểm đến không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @NotBlank(message = "Ảnh điểm đến không được để trống")
    private String imageUrl;

    @NotNull(message = "Trạng thái điểm đến không được để trống")
    private DestinationStatus status;

    @NotNull(message = "Vùng miền không được để trống")
    private Region region;

    private Long toursCount;

    public DestinationDTO() {}

    public DestinationDTO(Long id, String name, String description, String imageUrl,
                          DestinationStatus status, Region region) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.region = region;
    }

    public DestinationDTO(Long id, String name, String description, String imageUrl,
                          DestinationStatus status, Region region, Long toursCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.region = region;
        this.toursCount = toursCount;
    }
}

