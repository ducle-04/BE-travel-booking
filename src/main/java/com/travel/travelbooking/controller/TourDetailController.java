package com.travel.travelbooking.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.dto.TourDetailDTO;
import com.travel.travelbooking.dto.TransportDTO;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.TourDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tours/{tourId}/details")
@RequiredArgsConstructor
public class TourDetailController {

    private final TourDetailService tourDetailService;
    private final ObjectMapper objectMapper;

    // 1. TẠO HOẶC CẬP NHẬT CHI TIẾT TOUR (ADMIN | STAFF)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> createOrUpdateTourDetail(
            @PathVariable Long tourId,
            @RequestParam("itinerary") String itinerary,
            @RequestParam("departurePoint") String departurePoint,
            @RequestParam("departureTime") String departureTime,
            @RequestParam("suitableFor") String suitableFor,
            @RequestParam("cancellationPolicy") String cancellationPolicy,
            @RequestParam("transports") String transportsJson,           // ← JSON: [{"name":"Xe VIP", "price":500000}]
            @RequestParam("selectedHotelIds") String selectedHotelIdsJson, // ← JSON: [1, 2, 3]
            @RequestParam(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos) throws IOException {

        // Parse JSON → List<TransportDTO>
        List<TransportDTO> transports = objectMapper.readValue(transportsJson, new TypeReference<>() {});

        // Parse JSON → List<Long>
        List<Long> selectedHotelIds = objectMapper.readValue(selectedHotelIdsJson, new TypeReference<>() {});

        TourDetailDTO dto = tourDetailService.createOrUpdateTourDetail(
                tourId,
                itinerary,
                departurePoint,
                departureTime,
                suitableFor,
                cancellationPolicy,
                transports,
                selectedHotelIds,
                additionalImages,
                videos
        );

        return ResponseEntity.ok(new ApiResponse<>("Cập nhật chi tiết tour thành công", dto));
    }

    // 2. LẤY CHI TIẾT TOUR (PUBLIC)
    @GetMapping
    public ResponseEntity<?> getTourDetail(@PathVariable Long tourId) {
        TourDetailDTO dto = tourDetailService.getTourDetailDTO(tourId);
        if (dto == null) {
            return ResponseEntity.ok(new ApiResponse<>("Chưa có chi tiết tour", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("Lấy chi tiết tour thành công", dto));
    }

    // 3. XÓA ẢNH PHỤ (ADMIN | STAFF)
    @DeleteMapping("/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteAdditionalImage(
            @PathVariable Long tourId,
            @RequestParam("imageUrl") String imageUrl) {

        tourDetailService.deleteAdditionalImage(tourId, imageUrl);
        return ResponseEntity.ok(new ApiResponse<>("Xóa ảnh phụ thành công", null));
    }

    // 4. XÓA VIDEO (ADMIN | STAFF)
    @DeleteMapping("/videos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteVideo(
            @PathVariable Long tourId,
            @RequestParam("videoUrl") String videoUrl) {

        tourDetailService.deleteVideo(tourId, videoUrl);
        return ResponseEntity.ok(new ApiResponse<>("Xóa video thành công", null));
    }
}