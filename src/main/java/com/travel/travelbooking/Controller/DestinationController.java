package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.DestinationDTO;
import com.travel.travelbooking.Entity.DestinationStatus;
import com.travel.travelbooking.Entity.Region;
import com.travel.travelbooking.Service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    @Autowired
    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public ResponseEntity<?> getAllDestinations() {
        List<DestinationDTO> destinations = destinationService.getAllDestinations();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lấy danh sách điểm đến thành công");
        response.put("destinations", destinations.stream()
                .filter(dto -> dto.getStatus() != DestinationStatus.DELETED)
                .toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDestinationById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID điểm đến không hợp lệ");
        }

        try {
            DestinationDTO destination = destinationService.getDestinationById(id);
            if (destination.getStatus() == DestinationStatus.DELETED) {
                return ResponseEntity.status(404).body("Điểm đến không khả dụng hoặc không tồn tại");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lấy thông tin điểm đến thành công");
            response.put("destination", destination);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Điểm đến không tồn tại với ID: " + id);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDestinationsByName(@RequestParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên điểm đến không được để trống");
        }

        try {
            List<DestinationDTO> destinations = destinationService.searchDestinationsByName(name).stream()
                    .filter(dto -> dto.getStatus() != DestinationStatus.DELETED)
                    .collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tìm kiếm điểm đến thành công");
            response.put("destinations", destinations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tìm kiếm điểm đến: " + e.getMessage());
        }
    }

    @GetMapping("/region")
    public ResponseEntity<?> getDestinationsByRegion(@RequestParam("region") Region region) {
        try {
            List<DestinationDTO> destinations = destinationService.getDestinationsByRegion(region);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lọc điểm đến theo khu vực thành công");
            response.put("region", region);
            response.put("destinations", destinations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lọc điểm đến: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> createDestination(@RequestBody DestinationDTO destinationDTO) {
        if (destinationDTO.getName() == null || destinationDTO.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên điểm đến không được để trống");
        }
        if (destinationDTO.getRegion() == null) {
            return ResponseEntity.badRequest().body("Khu vực (region) không được để trống");
        }

        try {
            DestinationDTO createdDestination = destinationService.createDestination(destinationDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo điểm đến thành công");
            response.put("destination", createdDestination);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo điểm đến: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi tạo điểm đến: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> updateDestination(@PathVariable Long id, @RequestBody DestinationDTO destinationDTO) {
        if (id == null || id <= 0 || destinationDTO.getName() == null || destinationDTO.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("ID hoặc tên điểm đến không hợp lệ");
        }
        if (destinationDTO.getRegion() == null) {
            return ResponseEntity.badRequest().body("Khu vực (region) không được để trống");
        }

        try {
            DestinationDTO updatedDestination = destinationService.updateDestination(id, destinationDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật điểm đến thành công");
            response.put("destination", updatedDestination);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật điểm đến: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Điểm đến không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi cập nhật điểm đến: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> deleteDestination(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID điểm đến không hợp lệ");
        }

        try {
            destinationService.deleteDestination(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Điểm đến không tồn tại với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server khi xóa điểm đến: " + e.getMessage());
        }
    }
}