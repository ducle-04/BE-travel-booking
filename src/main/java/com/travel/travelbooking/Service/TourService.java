// src/main/java/com/travel/travelbooking/Service/TourService.java
package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourDetailDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.Destination;
import com.travel.travelbooking.Entity.DestinationStatus;
import com.travel.travelbooking.Entity.Tour;
import com.travel.travelbooking.Entity.TourStatus;
import com.travel.travelbooking.Repository.DestinationRepository;
import com.travel.travelbooking.Repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class TourService {

    @Autowired private TourRepository tourRepository;
    @Autowired private DestinationRepository destinationRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private TourDetailService tourDetailService;

    // BATCH ENHANCE → DÙNG getTourDetailDTO()
    @Transactional(readOnly = true)
    private void enhanceWithTourDetails(List<TourDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        dtos.forEach(dto -> {
            TourDetailDTO detailDTO = tourDetailService.getTourDetailDTO(dto.getId());
            if (detailDTO != null) {
                dto.setTourDetail(detailDTO);
            }
        });
    }

    // CREATE TOUR
    @Transactional
    public TourDTO createTour(TourDTO tourDTO, MultipartFile imageFile) throws IOException {
        validateTourInput(tourDTO);

        Destination destination = destinationRepository.findByName(tourDTO.getDestinationName())
                .orElseThrow(() -> new IllegalArgumentException("Điểm đến không tồn tại với tên: " + tourDTO.getDestinationName()));

        if (destination.getStatus() == DestinationStatus.DELETED) {
            throw new IllegalArgumentException("Điểm đến đã bị xóa, không thể thêm tour");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile);
            tourDTO.setImageUrl(imageUrl);
        } else if (tourDTO.getImageUrl() != null && !tourDTO.getImageUrl().trim().isEmpty()) {
            validateImageUrl(tourDTO.getImageUrl());
        }

        if (tourRepository.findByNameContainingIgnoreCase(tourDTO.getName()).stream()
                .anyMatch(t -> t.getId() != null && !t.getId().equals(tourDTO.getId()))) {
            throw new IllegalArgumentException("Tour với tên '" + tourDTO.getName() + "' đã tồn tại");
        }

        Tour tour = toEntity(tourDTO);
        tour.setDestination(destination);
        Tour savedTour = tourRepository.save(tour);

        return tourRepository.findByIdWithCounts(savedTour.getId())
                .map(dto -> {
                    enhanceWithTourDetails(List.of(dto));
                    return dto;
                })
                .orElseThrow(() -> new RuntimeException("Lỗi khi lấy thông tin tour vừa tạo"));
    }

    // GET ALL TOURS
    @Transactional(readOnly = true)
    public List<TourDTO> getAllTours() {
        List<TourDTO> dtos = tourRepository.findAllWithCounts();
        enhanceWithTourDetails(dtos);
        return dtos;
    }

    // GET TOUR BY ID
    @Transactional(readOnly = true)
    public TourDTO getTourById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tour không hợp lệ");
        }
        TourDTO dto = tourRepository.findByIdWithCounts(id)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại với ID: " + id));
        enhanceWithTourDetails(List.of(dto));
        return dto;
    }

    // SEARCH BY NAME
    @Transactional(readOnly = true)
    public Page<TourDTO> searchToursByName(String name, int page) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tour không được để trống");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Số trang phải lớn hơn hoặc bằng 0");
        }
        Pageable pageable = PageRequest.of(page, 10);
        Page<TourDTO> result = tourRepository.findByNameContainingIgnoreCaseWithCountsAndPageable(name, pageable);
        enhanceWithTourDetails(result.getContent());
        return result;
    }

    // GET TOURS BY DESTINATION
    @Transactional(readOnly = true)
    public List<TourDTO> getToursByDestination(Long destinationId) {
        if (destinationId == null || destinationId <= 0) {
            throw new IllegalArgumentException("ID điểm đến không hợp lệ");
        }
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new IllegalArgumentException("Điểm đến không tồn tại với ID: " + destinationId));
        List<TourDTO> dtos = tourRepository.findByDestinationWithCounts(destination);
        enhanceWithTourDetails(dtos);
        return dtos;
    }

    // UPDATE TOUR
    @Transactional
    public TourDTO updateTour(Long id, TourDTO tourDTO, MultipartFile imageFile) throws IOException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tour không hợp lệ");
        }
        validateTourInput(tourDTO);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại với ID: " + id));

        if (!tour.getName().equalsIgnoreCase(tourDTO.getName()) &&
                tourRepository.findByNameContainingIgnoreCase(tourDTO.getName()).stream()
                        .anyMatch(t -> t.getId() != null && !t.getId().equals(id))) {
            throw new IllegalArgumentException("Tour với tên '" + tourDTO.getName() + "' đã tồn tại");
        }

        Destination destination = destinationRepository.findByName(tourDTO.getDestinationName())
                .orElseThrow(() -> new IllegalArgumentException("Điểm đến không tồn tại với tên: " + tourDTO.getDestinationName()));

        if (destination.getStatus() == DestinationStatus.DELETED) {
            throw new IllegalArgumentException("Điểm đến đã bị xóa, không thể cập nhật tour");
        }

        if (destination.getStatus() == DestinationStatus.INACTIVE && tourDTO.getStatus() == TourStatus.ACTIVE) {
            throw new IllegalArgumentException("Không thể kích hoạt tour vì điểm đến đang tạm ngưng hoạt động");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(imageFile);
            tour.setImageUrl(newImageUrl);
        } else if (tourDTO.getImageUrl() != null && !tourDTO.getImageUrl().trim().isEmpty()) {
            validateImageUrl(tourDTO.getImageUrl());
            tour.setImageUrl(tourDTO.getImageUrl());
        }

        tour.setName(tourDTO.getName());
        tour.setDestination(destination);
        tour.setDuration(tourDTO.getDuration());
        tour.setPrice(tourDTO.getPrice());
        tour.setDescription(tourDTO.getDescription());
        tour.setMaxParticipants(tourDTO.getMaxParticipants());
        if (tourDTO.getStatus() != null) {
            tour.setStatus(tourDTO.getStatus());
        }

        tourRepository.save(tour);

        return tourRepository.findByIdWithCounts(id)
                .map(dto -> {
                    enhanceWithTourDetails(List.of(dto));
                    return dto;
                })
                .orElseThrow(() -> new RuntimeException("Lỗi khi lấy thông tin tour vừa cập nhật"));
    }

    // DELETE TOUR (SOFT DELETE)
    @Transactional
    public void deleteTour(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID tour không hợp lệ");
        }
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại với ID: " + id));
        tour.setStatus(TourStatus.DELETED);
        tourRepository.save(tour);
    }

    // FILTER TOURS
    @Transactional(readOnly = true)
    public Page<TourDTO> getFilteredTours(String destinationName, TourStatus status, Double minPrice, Double maxPrice, int page) {
        if (page < 0) throw new IllegalArgumentException("Số trang phải lớn hơn hoặc bằng 0");
        if (minPrice != null && minPrice < 0) throw new IllegalArgumentException("Giá tối thiểu phải lớn hơn hoặc bằng 0");
        if (maxPrice != null && maxPrice < 0) throw new IllegalArgumentException("Giá tối đa phải lớn hơn hoặc bằng 0");
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("Giá tối thiểu không được lớn hơn giá tối đa");
        }

        Pageable pageable = PageRequest.of(page, 10);
        Page<TourDTO> result = tourRepository.findFilteredTours(destinationName, status, minPrice, maxPrice, pageable);
        enhanceWithTourDetails(result.getContent());
        return result;
    }

    // GET TOUR STATS
    public TourStatsDTO getTourStats() {
        return tourRepository.getTourStats();
    }

    // HELPER METHODS
    private void validateTourInput(TourDTO tourDTO) {
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
        if (tourDTO.getMaxParticipants() == null || tourDTO.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Số người tối đa phải lớn hơn 0");
        }
    }

    private void validateImageUrl(String imageUrl) {
        try {
            new URL(imageUrl).toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
        }
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
        tour.setMaxParticipants(dto.getMaxParticipants() != null && dto.getMaxParticipants() > 0 ? dto.getMaxParticipants() : 50);
        tour.setStatus(dto.getStatus() != null ? dto.getStatus() : TourStatus.ACTIVE);
        tour.setCreatedAt(dto.getCreatedAt());
        return tour;
    }
}