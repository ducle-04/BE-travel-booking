package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.TourStatus;
import com.travel.travelbooking.Payload.ApiResponse;
import com.travel.travelbooking.Service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    // 1. Lấy danh sách tất cả tour (có count bookings & reviews)
    @GetMapping
    public ResponseEntity<ApiResponse<List<TourDTO>>> getAllTours() {
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy danh sách tour thành công", tourService.getAllTours())
        );
    }

    // 2. Lấy chi tiết 1 tour theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TourDTO>> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy thông tin tour thành công", tourService.getTourById(id))
        );
    }

    // 3. Tìm kiếm tour theo tên (phân trang, page=0, size=10)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TourDTO>>> searchTours(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page) {

        Page<TourDTO> result = tourService.searchToursByName(name, page);
        return ResponseEntity.ok(
                new ApiResponse<>("Tìm kiếm tour thành công", result)
        );
    }

    // 4. Lấy danh sách tour theo điểm đến (destinationId)
    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<ApiResponse<List<TourDTO>>> getToursByDestination(@PathVariable Long destinationId) {
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy tour theo điểm đến thành công",
                        tourService.getToursByDestination(destinationId))
        );
    }

    // 5. Lọc tour nâng cao (destinationName, status, price range, phân trang)
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<TourDTO>>> filterTours(
            @RequestParam(required = false) String destinationName,
            @RequestParam(required = false) TourStatus status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page) {

        Page<TourDTO> result = tourService.getFilteredTours(destinationName, status, minPrice, maxPrice, page);
        return ResponseEntity.ok(
                new ApiResponse<>("Lọc tour thành công", result)
        );
    }

    // 6. Thống kê tổng quan tour
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TourStatsDTO>> getTourStats() {
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy thống kê tour thành công", tourService.getTourStats())
        );
    }

    // 7. Tạo tour mới (ADMIN | STAFF)
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourDTO>> createTour(
            @Valid @RequestPart("tour") TourDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        TourDTO created = tourService.createTour(dto, imageFile);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Tạo tour thành công", created));
    }

    // 8. Cập nhật tour (ADMIN | STAFF)
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourDTO>> updateTour(
            @PathVariable Long id,
            @Valid @RequestPart("tour") TourDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        TourDTO updated = tourService.updateTour(id, dto, imageFile);
        return ResponseEntity.ok(
                new ApiResponse<>("Cập nhật tour thành công", updated)
        );
    }

    // 9. Xóa mềm tour (ADMIN | STAFF)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Xóa tour thành công", null)
        );
    }
}