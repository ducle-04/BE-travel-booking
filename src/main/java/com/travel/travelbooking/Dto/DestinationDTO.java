package com.travel.travelbooking.Dto;

import com.travel.travelbooking.Entity.Region;
import lombok.Data;
import com.travel.travelbooking.Entity.DestinationStatus;

@Data
public class DestinationDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private DestinationStatus status;
    private Region region;
    private Long toursCount; // 🟢 số lượng tour đang hoạt động

    public DestinationDTO() {}

    public DestinationDTO(Long id, String name, String description, String imageUrl, DestinationStatus status, Region region) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.region = region;
    }

    // 🟢 Constructor dùng cho query có COUNT()
    public DestinationDTO(Long id, String name, String description, String imageUrl,
                          DestinationStatus status,Region regio, Long toursCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.region = regio;
        this.toursCount = toursCount;
    }
}
