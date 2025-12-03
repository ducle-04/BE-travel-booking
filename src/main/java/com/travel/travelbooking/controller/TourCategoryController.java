package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.TourCategoryDTO;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.TourCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour-categories")
@RequiredArgsConstructor
public class TourCategoryController {

    private final TourCategoryService service;

    // Cho khách hàng
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TourCategoryDTO>>> getActive() {
        return ResponseEntity.ok(new ApiResponse<>("Thành công", service.getActiveCategories()));
    }

    // Cho admin - PHÂN TRANG
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Page<TourCategoryDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TourCategoryDTO> result = service.getAllCategoriesPaged(pageable);

        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách loại tour thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourCategoryDTO>> create(@Valid @RequestBody TourCategoryDTO dto) {
        TourCategoryDTO created = service.create(dto);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Tạo loại tour thành công", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourCategoryDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody TourCategoryDTO dto) {

        TourCategoryDTO updated = service.update(id, dto);
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa loại tour thành công", null));
    }
}