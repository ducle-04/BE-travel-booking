package com.travel.travelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.dto.StartDateAvailabilityDTO;
import com.travel.travelbooking.dto.TourDTO;
import com.travel.travelbooking.dto.TourStatsDTO;
import com.travel.travelbooking.entity.Tour;
import com.travel.travelbooking.entity.TourStatus;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.TourRepository;
import com.travel.travelbooking.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final ObjectMapper objectMapper;

    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;

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
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page) {

        Page<TourDTO> result = tourService.getFilteredTours(
                destinationName, status, minPrice, maxPrice, categoryId, page   // ⭐ THÊM categoryId
        );

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

    // 7: LẤY TOUR THEO LOẠI TOUR
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<TourDTO>>> getToursByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<TourDTO> result = tourService.getToursByCategoryPaged(categoryId, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>("Lấy tour theo loại thành công", result)
        );
    }

    // 8. Tạo tour mới (ADMIN | STAFF)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourDTO>> createTour(
            @RequestPart("tour") String tourJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        TourDTO dto = objectMapper.readValue(tourJson, TourDTO.class); // 👈 DÙNG mapper CỦA SPRING

        TourDTO created = tourService.createTour(dto, imageFile);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Tạo tour thành công", created));
    }

    // 9. Cập nhật tour (ADMIN | STAFF)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<TourDTO>> updateTour(
            @PathVariable Long id,
            @RequestPart("tour") String tourJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        TourDTO dto = objectMapper.readValue(tourJson, TourDTO.class); // 👈 VẪN DÙNG mapper CỦA SPRING

        TourDTO updated = tourService.updateTour(id, dto, imageFile);
        return ResponseEntity.ok(
                new ApiResponse<>("Cập nhật tour thành công", updated)
        );
    }

    // 10. Xóa mềm tour
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Xóa tour thành công", null)
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/{tourId}/start-dates")
    public ResponseEntity<List<StartDateAvailabilityDTO>> getStartDatesWithAvailability(@PathVariable Long tourId) {

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));

        if (tour.getStatus() != TourStatus.ACTIVE) {
            return ResponseEntity.ok(List.of());
        }

        List<StartDateAvailabilityDTO> result = tour.getStartDates().stream()
                .map(ts -> {
                    long booked = bookingRepository.getParticipantsByStartDate(ts.getId());
                    int capacity = ts.getCapacity();
                    int remaining = capacity - (int) booked;

                    StartDateAvailabilityDTO dto = new StartDateAvailabilityDTO();
                    dto.setDate(ts.getStartDate());
                    dto.setFormattedDate(formatVietnameseDate(ts.getStartDate()));
                    dto.setRemainingSeats(Math.max(remaining, 0));
                    dto.setAvailable(remaining > 0);

                    return dto;
                })
                .sorted(Comparator.comparing(StartDateAvailabilityDTO::getDate))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private String formatVietnameseDate(LocalDate date) {
        if (date == null) return "";
        String[] weekdays = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        int dayOfWeek = date.getDayOfWeek().getValue() % 7; // Chủ nhật = 0
        String formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return formatted + " (" + weekdays[dayOfWeek] + ")";
    }
}