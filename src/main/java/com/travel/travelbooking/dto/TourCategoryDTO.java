package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.CategoryStatus;
import lombok.Data;

@Data
public class TourCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer displayOrder;
    private CategoryStatus status;

    public TourCategoryDTO() {}

    public TourCategoryDTO(Long id, String name, String description, String icon, Integer displayOrder, CategoryStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.displayOrder = displayOrder;
        this.status = status;
        this.status = status;
    }
}