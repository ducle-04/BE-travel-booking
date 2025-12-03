package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.TourCategoryDTO;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.TourCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour-categories")
@RequiredArgsConstructor
public class TourCategoryController {

    private final TourCategoryService service;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TourCategoryDTO>>> getActive() {
        return ResponseEntity.ok(new ApiResponse<>("Thành công", service.getActiveCategories()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<TourCategoryDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>("Thành công", service.getAllCategories()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourCategoryDTO>> create(@Valid @RequestBody TourCategoryDTO dto) {
        return ResponseEntity.status(201).body(new ApiResponse<>("Tạo loại tour thành công", service.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourCategoryDTO>> update(@PathVariable Long id, @Valid @RequestBody TourCategoryDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật thành công", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa loại tour thành công", null));
    }
}