// src/main/java/com/travel/travelbooking/Controller/TourDetailController.java
package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.TourDetailDTO;
import com.travel.travelbooking.Service.TourDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tours/{tourId}/details")
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
public class TourDetailController {

    private final TourDetailService tourDetailService;

    @Autowired
    public TourDetailController(TourDetailService tourDetailService) {
        this.tourDetailService = tourDetailService;
    }

    // CREATE / UPDATE (TRẢ DTO)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createOrUpdateTourDetail(
            @PathVariable Long tourId,
            @RequestParam("transportation") String transportation,
            @RequestParam("itinerary") String itinerary,
            @RequestParam("departurePoint") String departurePoint,
            @RequestParam("departureTime") String departureTime,
            @RequestParam("suitableFor") String suitableFor,
            @RequestParam("cancellationPolicy") String cancellationPolicy,
            @RequestParam(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos) {

        try {
            TourDetailDTO dto = tourDetailService.createOrUpdateTourDetail(
                    tourId, transportation, itinerary, departurePoint, departureTime,
                    suitableFor, cancellationPolicy, additionalImages, videos);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật chi tiết tour thành công",
                    "detail", dto
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // GET DETAIL (PUBLIC + DTO)
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getTourDetail(@PathVariable Long tourId) {
        try {
            TourDetailDTO dto = tourDetailService.getTourDetailDTO(tourId);
            if (dto == null) {
                return ResponseEntity.ok(Map.of("message", "Chưa có chi tiết tour"));
            }
            return ResponseEntity.ok(Map.of("detail", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // DELETE IMAGE
    @DeleteMapping("/images")
    public ResponseEntity<?> deleteAdditionalImage(
            @PathVariable Long tourId,
            @RequestParam("imageUrl") String imageUrl) {
        try {
            tourDetailService.deleteAdditionalImage(tourId, imageUrl);
            return ResponseEntity.ok(Map.of("message", "Xóa ảnh phụ thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }

    // DELETE VIDEO
    @DeleteMapping("/videos")
    public ResponseEntity<?> deleteVideo(
            @PathVariable Long tourId,
            @RequestParam("videoUrl") String videoUrl) {
        try {
            tourDetailService.deleteVideo(tourId, videoUrl);
            return ResponseEntity.ok(Map.of("message", "Xóa video thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}