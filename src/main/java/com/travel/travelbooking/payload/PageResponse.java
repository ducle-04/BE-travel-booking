package com.travel.travelbooking.payload;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,           // trang hiện tại (bắt đầu từ 0)
        int size,           // số phần tử mỗi trang
        long totalElements,
        int totalPages
) {
    // Constructor tiện lợi: chuyển trực tiếp từ Spring Data Page
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}