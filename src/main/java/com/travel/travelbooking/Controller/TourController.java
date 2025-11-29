package com.travel.travelbooking.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.TourStatus;
import com.travel.travelbooking.Payload.ApiResponse;
import com.travel.travelbooking.Service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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

    // 1. Lấy danh sách tất cả tour (có count bookings & reviews + category)
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

    // 3. Tìm kiếm tour theo tên (phân trang)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TourDTO>>> searchTours(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page) {

        Page<TourDTO> result = tourService.searchToursByName(name, page);
        return ResponseEntity.ok(
                new ApiResponse<>("Tìm kiếm tour thành công", result)
        );
    }

    // 4. Lấy tour theo điểm đến
    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<ApiResponse<List<TourDTO>>> getToursByDestination(@PathVariable Long destinationId) {
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy tour theo điểm đến thành công",
                        tourService.getToursByDestination(destinationId))
        );
    }

    // 5. Lọc tour nâng cao
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

    // 6. Thống kê tour
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TourStatsDTO>> getTourStats() {
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy thống kê tour thành công", tourService.getTourStats())
        );
    }

    // MỚI: LẤY TOUR THEO LOẠI TOUR (categoryId) – RẤT HAY DÙNG Ở TRANG CHỦ
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<TourDTO>>> getToursByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<TourDTO> tours = tourService.getToursByCategory(categoryId);
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy danh sách tour theo loại thành công", tours)
        );
    }

    // Bonus: Lấy tour theo loại + phân trang (nếu cần nhiều hơn 10 tour)
    @GetMapping("/category/{categoryId}/paged")
    public ResponseEntity<ApiResponse<Page<TourDTO>>> getToursByCategoryPaged(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<TourDTO> result = tourService.getToursByCategoryPaged(categoryId, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy tour theo loại (có phân trang) thành công", result)
        );
    }

    // 7. Tạo tour mới (ADMIN | STAFF)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourDTO>> createTour(
            @RequestPart("tour") String tourJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TourDTO dto = mapper.readValue(tourJson, TourDTO.class);

        TourDTO created = tourService.createTour(dto, imageFile);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Tạo tour thành công", created));
    }

    // 8. Cập nhật tour (ADMIN | STAFF)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourDTO>> updateTour(
            @PathVariable Long id,
            @RequestPart("tour") String tourJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TourDTO dto = mapper.readValue(tourJson, TourDTO.class);

        TourDTO updated = tourService.updateTour(id, dto, imageFile);
        return ResponseEntity.ok(
                new ApiResponse<>("Cập nhật tour thành công", updated)
        );
    }

    // 9. Xóa mềm tour
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Xóa tour thành công", null)
        );
    }
}