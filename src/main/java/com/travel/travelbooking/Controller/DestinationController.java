package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.DestinationDTO;
import com.travel.travelbooking.Entity.Region;
import com.travel.travelbooking.Payload.ApiResponse;
import com.travel.travelbooking.Service.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public ResponseEntity<?> getAllDestinations() {
        return ResponseEntity.ok(new ApiResponse<>(
                "Lấy danh sách điểm đến thành công",
                destinationService.getAllDestinations()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDestinationById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Lấy thông tin điểm đến thành công",
                destinationService.getDestinationById(id)
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDestinations(@RequestParam String name) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Tìm kiếm điểm đến thành công",
                destinationService.searchDestinationsByName(name)
        ));
    }

    @GetMapping("/region")
    public ResponseEntity<?> getByRegion(@RequestParam Region region) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Lọc điểm đến theo vùng thành công",
                destinationService.getDestinationsByRegion(region)
        ));
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> createDestination(
            @Valid @RequestPart("destination") DestinationDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        var created = destinationService.createDestination(dto, imageFile);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Tạo điểm đến thành công", created));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> updateDestination(
            @PathVariable Long id,
            @Valid @RequestPart("destination") DestinationDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        var updated = destinationService.updateDestination(id, dto, imageFile);
        return ResponseEntity.ok(new ApiResponse<>("Cập nhật điểm đến thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteDestination(@PathVariable Long id) {
        destinationService.deleteDestination(id);
        return ResponseEntity.ok(new ApiResponse<>("Xóa điểm đến thành công", null));
    }
}