package com.travel.travelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.dto.HotelDTO;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.payload.PageResponse;
import com.travel.travelbooking.service.HotelService;
import com.travel.travelbooking.service.HotelStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<HotelDTO>> createHotel(
            @RequestPart("hotel") String hotelJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) throws IOException {

        HotelDTO dto = objectMapper.readValue(hotelJson, HotelDTO.class);
        HotelDTO created = hotelService.createHotel(dto, images, videos);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Tạo khách sạn thành công!", created));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<HotelDTO>> updateHotel(
            @PathVariable Long id,
            @RequestPart("hotel") String hotelJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) throws IOException {

        HotelDTO dto = objectMapper.readValue(hotelJson, HotelDTO.class);
        HotelDTO updated = hotelService.updateHotel(id, dto, images, videos);
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật thành công!", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa khách sạn thành công!", null));
    }

    // === THÊM: XÓA ẢNH RIÊNG LẺ ===
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteHotelImage(
            @PathVariable Long id,
            @RequestParam("imageUrl") String imageUrl) {

        hotelService.deleteHotelImage(id, imageUrl);
        return ResponseEntity.ok(new ApiResponse<>("Xóa ảnh thành công!", null));
    }

    // === THÊM: XÓA VIDEO RIÊNG LẺ ===
    @DeleteMapping("/{id}/videos")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteHotelVideo(
            @PathVariable Long id,
            @RequestParam("videoUrl") String videoUrl) {

        hotelService.deleteHotelVideo(id, videoUrl);
        return ResponseEntity.ok(new ApiResponse<>("Xóa video thành công!", null));
    }

    // LỌC + PHÂN TRANG
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<HotelDTO>>> getHotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer starRating) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<HotelDTO> result = hotelService.searchHotels(pageable, name, address, status, starRating);
        PageResponse<HotelDTO> response = PageResponse.of(result);

        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách thành công", response));
    }

    // THỐNG KÊ
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<HotelStats>> getStats() {
        return ResponseEntity.ok(new ApiResponse<>("Thống kê khách sạn", hotelService.getHotelStats()));
    }
}