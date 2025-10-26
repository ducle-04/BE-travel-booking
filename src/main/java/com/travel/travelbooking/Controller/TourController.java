package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.TourStatus;
import com.travel.travelbooking.Service.TourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;

    @Autowired
    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTours() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách tour thành công");
            response.put("tours", tourService.getAllTours());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi lấy danh sách tour: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTourById(@PathVariable Long id) {
        try {
            TourDTO tour = tourService.getTourById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy thông tin tour thành công");
            response.put("tour", tour);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Tour không tồn tại với ID: " + id);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchToursByName(
            @RequestParam("name") String name,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        try {
            Page<TourDTO> toursPage = tourService.searchToursByName(name, page);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tìm kiếm tour thành công");
            response.put("tours", toursPage.getContent());
            response.put("currentPage", toursPage.getNumber());
            response.put("totalPages", toursPage.getTotalPages());
            response.put("totalItems", toursPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tìm kiếm tour: " + e.getMessage());
        }
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<?> getToursByDestination(@PathVariable Long destinationId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy danh sách tour theo điểm đến thành công");
            response.put("tours", tourService.getToursByDestination(destinationId));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi lấy tour theo điểm đến: " + e.getMessage());
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterTours(
            @RequestParam(value = "destinationName", required = false) String destinationName,
            @RequestParam(value = "status", required = false) TourStatus status,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        try {
            Page<TourDTO> toursPage = tourService.getFilteredTours(destinationName, status, minPrice, maxPrice, page);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lọc tour thành công");
            response.put("tours", toursPage.getContent());
            response.put("currentPage", toursPage.getNumber());
            response.put("totalPages", toursPage.getTotalPages());
            response.put("totalItems", toursPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi lọc tour: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getTourStats() {
        try {
            TourStatsDTO stats = tourService.getTourStats();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy thống kê tour thành công");
            response.put("stats", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi lấy thống kê tour: " + e.getMessage());
        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> createTour(
            @RequestPart("tour") String tourJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TourDTO tourDTO = mapper.readValue(tourJson, TourDTO.class);
            if (tourDTO.getName() == null || tourDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên tour không được để trống");
            }
            if (tourDTO.getDestinationName() == null || tourDTO.getDestinationName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên điểm đến không được để trống");
            }
            if (tourDTO.getDuration() == null || tourDTO.getDuration().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Thời gian tour không được để trống");
            }
            if (tourDTO.getPrice() == null || tourDTO.getPrice() <= 0) {
                return ResponseEntity.badRequest().body("Giá tour phải lớn hơn 0");
            }
            TourDTO createdTour = tourService.createTour(tourDTO, imageFile);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo tour thành công");
            response.put("tour", createdTour);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo tour: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tạo tour: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> updateTour(
            @PathVariable Long id,
            @RequestPart("tour") String tourJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TourDTO tourDTO = mapper.readValue(tourJson, TourDTO.class);
            if (tourDTO.getName() == null || tourDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên tour không được để trống");
            }
            if (tourDTO.getDestinationName() == null || tourDTO.getDestinationName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên điểm đến không được để trống");
            }
            if (tourDTO.getDuration() == null || tourDTO.getDuration().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Thời gian tour không được để trống");
            }
            if (tourDTO.getPrice() == null || tourDTO.getPrice() <= 0) {
                return ResponseEntity.badRequest().body("Giá tour phải lớn hơn 0");
            }
            TourDTO updatedTour = tourService.updateTour(id, tourDTO, imageFile);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật tour thành công");
            response.put("tour", updatedTour);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật tour: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Tour không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi cập nhật tour: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteTour(@PathVariable Long id) {
        try {
            tourService.deleteTour(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Tour không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi xóa tour: " + e.getMessage());
        }
    }
}