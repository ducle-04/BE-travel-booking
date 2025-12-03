package com.travel.travelbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Transport {
    @Column(nullable = false, length = 100)
    private String name; // "Xe VIP 29 chỗ", "Máy bay Bamboo"

    @Column(nullable = false)
    private Double price; // Giá thêm (nếu có)
}