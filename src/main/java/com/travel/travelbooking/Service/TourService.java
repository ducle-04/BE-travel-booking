package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Entity.Destination;
import com.travel.travelbooking.Entity.Tour;
import com.travel.travelbooking.Entity.TourStatus;
import com.travel.travelbooking.Repository.DestinationRepository;
import com.travel.travelbooking.Repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private TourDTO toDTO(Tour tour) {
        TourDTO dto = new TourDTO();
        dto.setId(tour.getId());
        dto.setName(tour.getName());
        dto.setDestinationId(tour.getDestination().getId());
        dto.setDestinationName(tour.getDestination().getName());
        dto.setDuration(tour.getDuration());
        dto.setPrice(tour.getPrice());
        dto.setImageUrl(tour.getImageUrl());
        dto.setDescription(tour.getDescription());
        dto.setAverageRating(tour.getAverageRating());
        dto.setTotalParticipants(tour.getTotalParticipants() != null ? tour.getTotalParticipants() : 0);
        dto.setStatus(tour.getStatus());
        dto.setCreatedAt(tour.getCreatedAt());
        return dto;
    }

    private Tour toEntity(TourDTO dto) {
        Tour tour = new Tour();
        tour.setId(dto.getId());
        tour.setName(dto.getName());
        tour.setDuration(dto.getDuration());
        tour.setPrice(dto.getPrice());
        tour.setImageUrl(dto.getImageUrl());
        tour.setDescription(dto.getDescription());
        tour.setAverageRating(dto.getAverageRating());
        tour.setTotalParticipants(dto.getTotalParticipants() != null ? dto.getTotalParticipants() : 0);
        tour.setStatus(dto.getStatus() != null ? dto.getStatus() : TourStatus.ACTIVE);
        tour.setCreatedAt(dto.getCreatedAt());
        return tour;
    }

    public TourDTO createTour(TourDTO tourDTO, MultipartFile imageFile) throws IOException {
        if (tourDTO.getName() == null || tourDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tour không được để trống");
        }
        if (tourDTO.getDestinationName() == null || tourDTO.getDestinationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên điểm đến không được để trống");
        }
        if (tourDTO.getDuration() == null || tourDTO.getDuration().trim().isEmpty()) {
            throw new IllegalArgumentException("Thời gian tour không được để trống");
        }
        if (tourDTO.getPrice() == null || tourDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá tour phải lớn hơn 0");
        }

        // Tìm Destination theo tên
        Destination destination = destinationRepository.findByName(tourDTO.getDestinationName())
                .orElseThrow(() -> new IllegalArgumentException("Điểm đến không tồn tại với tên: " + tourDTO.getDestinationName()));
        if (destination.getStatus() == com.travel.travelbooking.Entity.DestinationStatus.DELETED) {
            throw new IllegalArgumentException("Điểm đến đã bị xóa, không thể thêm tour");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile);
            tourDTO.setImageUrl(imageUrl);
        } else if (tourDTO.getImageUrl() != null && !tourDTO.getImageUrl().trim().isEmpty()) {
            try {
                new URL(tourDTO.getImageUrl()).toURI();
            } catch (Exception e) {
                throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
            }
        }

        if (tourRepository.findByNameContainingIgnoreCase(tourDTO.getName()).stream()
                .anyMatch(tour -> !tour.getId().equals(tourDTO.getId()))) {
            throw new IllegalArgumentException("Tour với tên '" + tourDTO.getName() + "' đã tồn tại");
        }

        Tour tour = toEntity(tourDTO);
        tour.setDestination(destination);
        Tour savedTour = tourRepository.save(tour);
        return tourRepository.findByIdWithCounts(savedTour.getId())
                .orElseThrow(() -> new RuntimeException("Lỗi khi lấy thông tin tour vừa tạo"));
    }

    public List<TourDTO> getAllTours() {
        return tourRepository.findAllWithCounts();
    }

    public TourDTO getTourById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tour không hợp lệ");
        }
        return tourRepository.findByIdWithCounts(id)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại với ID: " + id));
    }

    public List<TourDTO> searchToursByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tour không được để trống");
        }
        return tourRepository.findByNameContainingIgnoreCaseWithCounts(name);
    }

    public List<TourDTO> getToursByDestination(Long destinationId) {
        if (destinationId == null || destinationId <= 0) {
            throw new IllegalArgumentException("ID điểm đến không hợp lệ");
        }
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new IllegalArgumentException("Điểm đến không tồn tại với ID: " + destinationId));
        return tourRepository.findByDestinationWithCounts(destination);
    }

    public TourDTO updateTour(Long id, TourDTO tourDTO, MultipartFile imageFile) throws IOException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tour không hợp lệ");
        }
        if (tourDTO.getName() == null || tourDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tour không được để trống");
        }
        if (tourDTO.getDestinationName() == null || tourDTO.getDestinationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên điểm đến không được để trống");
        }
        if (tourDTO.getDuration() == null || tourDTO.getDuration().trim().isEmpty()) {
            throw new IllegalArgumentException("Thời gian tour không được để trống");
        }
        if (tourDTO.getPrice() == null || tourDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá tour phải lớn hơn 0");
        }

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại với ID: " + id));

        // Kiểm tra trùng tên tour
        if (!tour.getName().equalsIgnoreCase(tourDTO.getName()) &&
                tourRepository.findByNameContainingIgnoreCase(tourDTO.getName()).stream()
                        .anyMatch(t -> !t.getId().equals(id))) {
            throw new IllegalArgumentException("Tour với tên '" + tourDTO.getName() + "' đã tồn tại");
        }

        // Tìm điểm đến theo tên
        Destination destination = destinationRepository.findByName(tourDTO.getDestinationName())
                .orElseThrow(() -> new IllegalArgumentException("Điểm đến không tồn tại với tên: " + tourDTO.getDestinationName()));

        if (destination.getStatus() == com.travel.travelbooking.Entity.DestinationStatus.DELETED) {
            throw new IllegalArgumentException("Điểm đến đã bị xóa, không thể cập nhật tour");
        }

        // ✅ Chặn bật Active nếu Destination đang INACTIVE
        if (destination.getStatus() == com.travel.travelbooking.Entity.DestinationStatus.INACTIVE &&
                tourDTO.getStatus() == TourStatus.ACTIVE) {
            throw new IllegalArgumentException("Không thể kích hoạt tour vì điểm đến đang tạm ngưng hoạt động");
        }

        // Cập nhật ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(imageFile);
            tour.setImageUrl(newImageUrl);
        } else if (tourDTO.getImageUrl() != null && !tourDTO.getImageUrl().trim().isEmpty()) {
            try {
                new URL(tourDTO.getImageUrl()).toURI();
                tour.setImageUrl(tourDTO.getImageUrl());
            } catch (Exception e) {
                throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
            }
        }

        // Cập nhật các trường hợp khác
        tour.setName(tourDTO.getName());
        tour.setDestination(destination);
        tour.setDuration(tourDTO.getDuration());
        tour.setPrice(tourDTO.getPrice());
        tour.setDescription(tourDTO.getDescription());

        // ✅ Nếu FE không truyền status thì giữ nguyên
        if (tourDTO.getStatus() != null) {
            tour.setStatus(tourDTO.getStatus());
        }

        // Lưu lại
        tourRepository.save(tour);

        return tourRepository.findByIdWithCounts(id)
                .orElseThrow(() -> new RuntimeException("Lỗi khi lấy thông tin tour vừa cập nhật"));
    }


    @Transactional
    public void deleteTour(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tour không hợp lệ");
        }
        Optional<Tour> tourOptional = tourRepository.findById(id);
        if (tourOptional.isPresent()) {
            Tour tour = tourOptional.get();
            tour.setStatus(TourStatus.DELETED);
            tourRepository.save(tour);
        } else {
            throw new RuntimeException("Tour không tồn tại với ID: " + id);
        }
    }
}