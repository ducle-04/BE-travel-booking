package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.TourDetailDTO;
import com.travel.travelbooking.Payload.ApiResponse;
import com.travel.travelbooking.Service.TourDetailService;
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

    // 1 Tạo hoặc cập nhật chi tiết tour
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> createOrUpdateTourDetail(
            @PathVariable Long tourId,
            @RequestParam("transportation") String transportation,
            @RequestParam("itinerary") String itinerary,
            @RequestParam("departurePoint") String departurePoint,
            @RequestParam("departureTime") String departureTime,
            @RequestParam("suitableFor") String suitableFor,
            @RequestParam("cancellationPolicy") String cancellationPolicy,
            @RequestParam(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos) throws IOException {

        TourDetailDTO dto = tourDetailService.createOrUpdateTourDetail(
                tourId, transportation, itinerary, departurePoint, departureTime,
                suitableFor, cancellationPolicy, additionalImages, videos);

        return ResponseEntity.ok(new ApiResponse<>("Cập nhật chi tiết tour thành công", dto));
    }

    // 2 Lấy chi tiết tour (public)
    @GetMapping
    public ResponseEntity<?> getTourDetail(@PathVariable Long tourId) {
        TourDetailDTO dto = tourDetailService.getTourDetailDTO(tourId);
        if (dto == null) {
            return ResponseEntity.ok(new ApiResponse<>("Chưa có chi tiết tour", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("Lấy chi tiết tour thành công", dto));
    }

    // 3 Xóa ảnh phụ
    @DeleteMapping("/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteAdditionalImage(
            @PathVariable Long tourId,
            @RequestParam("imageUrl") String imageUrl) {

        tourDetailService.deleteAdditionalImage(tourId, imageUrl);
        return ResponseEntity.ok(new ApiResponse<>("Xóa ảnh phụ thành công", null));
    }

    // 4 Xóa video
    @DeleteMapping("/videos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteVideo(
            @PathVariable Long tourId,
            @RequestParam("videoUrl") String videoUrl) {

        tourDetailService.deleteVideo(tourId, videoUrl);
        return ResponseEntity.ok(new ApiResponse<>("Xóa video thành công", null));
    }
}