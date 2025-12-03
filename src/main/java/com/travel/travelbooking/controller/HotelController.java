package com.travel.travelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.dto.HotelDTO;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> createHotel(
            @RequestPart("hotel") String hotelJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) throws IOException {

        HotelDTO dto = objectMapper.readValue(hotelJson, HotelDTO.class);
        HotelDTO created = hotelService.createHotel(dto, images, videos);
        return ResponseEntity.status(201).body(new ApiResponse<>("Tạo khách sạn thành công", created));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> updateHotel(
            @PathVariable Long id,
            @RequestPart("hotel") String hotelJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) throws IOException {

        HotelDTO dto = objectMapper.readValue(hotelJson, HotelDTO.class);
        HotelDTO updated = hotelService.updateHotel(id, dto, images, videos);
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật khách sạn thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa khách sạn thành công", null));
    }

    @GetMapping
    public ResponseEntity<?> getAllHotels() {
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách khách sạn", hotelService.getAllHotels()));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchHotels(@RequestParam String name, @RequestParam(defaultValue = "0") int page) {
        Page<HotelDTO> result = hotelService.searchHotels(name, page);
        return ResponseEntity.ok(new ApiResponse<>("Tìm khách sạn", result));
    }
}