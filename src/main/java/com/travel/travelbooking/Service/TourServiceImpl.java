package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.*;
import com.travel.travelbooking.Exception.ResourceNotFoundException;
import com.travel.travelbooking.Repository.DestinationRepository;
import com.travel.travelbooking.Repository.TourCategoryRepository;
import com.travel.travelbooking.Repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private final TourCategoryRepository tourCategoryRepository;
    private final CloudinaryService cloudinaryService;
    private final TourDetailService tourDetailService;

    // ====================== PUBLIC METHODS ======================

    @Override
    @Transactional
    public TourDTO createTour(TourDTO dto, MultipartFile imageFile) throws IOException {
        validateImage(imageFile, dto.getImageUrl());
        validateUniqueName(dto.getName(), null);

        Destination dest = findDestinationByName(dto.getDestinationName());
        validateDestinationActive(dest);

        TourCategory category = findCategoryByName(dto.getCategoryName());
        validateCategoryActive(category);

        dto.setImageUrl(resolveImageUrl(imageFile, dto.getImageUrl()));

        Tour tour = toEntity(dto);
        tour.setDestination(dest);
        tour.setCategory(category);

        tour = tourRepository.save(tour);
        return findWithEnhancements(tour.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourDTO> getAllTours() {
        List<TourDTO> dtos = tourRepository.findAllWithCounts();
        enhanceWithDetails(dtos);
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public TourDTO getTourById(Long id) {
        validateId(id);
        TourDTO dto = tourRepository.findByIdWithCounts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));
        enhanceWithDetails(List.of(dto));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourDTO> searchToursByName(String name, int page) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tìm kiếm không được để trống");
        }
        Pageable pageable = PageRequest.of(page, 10);
        Page<TourDTO> result = tourRepository.findByNameContainingIgnoreCaseWithCountsAndPageable(name, pageable);
        enhanceWithDetails(result.getContent());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourDTO> getToursByDestination(Long destinationId) {
        validateId(destinationId);
        Destination dest = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Điểm đến không tồn tại"));
        List<TourDTO> dtos = tourRepository.findByDestinationWithCounts(dest);
        enhanceWithDetails(dtos);
        return dtos;
    }

    @Override
    @Transactional
    public TourDTO updateTour(Long id, TourDTO dto, MultipartFile imageFile) throws IOException {
        validateId(id);
        validateImage(imageFile, dto.getImageUrl());
        validateUniqueName(dto.getName(), id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));

        Destination dest = findDestinationByName(dto.getDestinationName());
        validateDestinationActive(dest);

        TourCategory category = findCategoryByName(dto.getCategoryName());
        validateCategoryActive(category);

        if (dest.getStatus() == DestinationStatus.INACTIVE && dto.getStatus() == TourStatus.ACTIVE) {
            throw new IllegalArgumentException("Không thể kích hoạt tour khi điểm đến tạm ngưng");
        }

        String imageUrl = resolveImageUrl(imageFile, dto.getImageUrl());
        if (imageUrl != null) {
            tour.setImageUrl(imageUrl);
        }

        updateEntity(tour, dto, dest, category);
        tourRepository.save(tour);

        return findWithEnhancements(id);
    }

    @Override
    @Transactional
    public void deleteTour(Long id) {
        validateId(id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));
        tour.setStatus(TourStatus.DELETED);
        tourRepository.save(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourDTO> getFilteredTours(String destinationName, TourStatus status,
                                          Double minPrice, Double maxPrice, int page) {
        validatePriceRange(minPrice, maxPrice);
        Pageable pageable = PageRequest.of(page, 10);
        Page<TourDTO> result = tourRepository.findFilteredTours(destinationName, status, minPrice, maxPrice, pageable);
        enhanceWithDetails(result.getContent());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public TourStatsDTO getTourStats() {
        return tourRepository.getTourStats();
    }

    // Lấy tour theo loại (không phân trang)
    @Override
    @Transactional(readOnly = true)
    public List<TourDTO> getToursByCategory(Long categoryId) {
        validateId(categoryId);
        List<TourDTO> dtos = tourRepository.findByCategoryIdWithCounts(categoryId);
        enhanceWithDetails(dtos);
        return dtos;
    }

    // Lấy tour theo loại + phân trang
    @Override
    @Transactional(readOnly = true)
    public Page<TourDTO> getToursByCategoryPaged(Long categoryId, int page, int size) {
        validateId(categoryId);

        Pageable pageable = PageRequest.of(page, size);
        Page<TourDTO> result = tourRepository.findByCategoryIdWithCountsPaged(categoryId, pageable);

        enhanceWithDetails(result.getContent());
        return result;
    }

    // ====================== PRIVATE HELPERS ======================

    private void enhanceWithDetails(List<TourDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        dtos.forEach(dto -> dto.setTourDetail(tourDetailService.getTourDetailDTO(dto.getId())));
    }

    private TourDTO findWithEnhancements(Long id) {
        TourDTO dto = tourRepository.findByIdWithCounts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));
        enhanceWithDetails(List.of(dto));
        return dto;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID không hợp lệ");
    }

    private void validateUniqueName(String name, Long excludeId) {
        tourRepository.findByNameContainingIgnoreCase(name).stream()
                .filter(t -> t.getStatus() != TourStatus.DELETED)
                .filter(t -> excludeId == null || !t.getId().equals(excludeId))
                .findFirst()
                .ifPresent(t -> { throw new IllegalArgumentException("Tên tour đã tồn tại"); });
    }

    private Destination findDestinationByName(String name) {
        return destinationRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Điểm đến '" + name + "' không tồn tại"));
    }

    private void validateDestinationActive(Destination dest) {
        if (dest.getStatus() == DestinationStatus.DELETED) {
            throw new IllegalArgumentException("Điểm đến đã bị xóa, không thể sử dụng");
        }
    }

    private TourCategory findCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại tour không được để trống");
        }
        return tourCategoryRepository.findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Loại tour '" + name + "' không tồn tại"));
    }

    private void validateCategoryActive(TourCategory category) {
        if (category.getStatus() == CategoryStatus.INACTIVE) {
            throw new IllegalArgumentException("Loại tour '" + category.getName() + "' đã bị vô hiệu hóa");
        }
    }

    private void validateImage(MultipartFile file, String url) throws IOException {
        if (file != null && !file.isEmpty()) {
            if (!file.getContentType().startsWith("image/")) throw new IllegalArgumentException("File phải là ảnh");
            if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Ảnh không quá 5MB");
        } else if (url != null && !url.trim().isEmpty()) {
            try { new URL(url).toURI(); } catch (Exception e) {
                throw new IllegalArgumentException("URL ảnh không hợp lệ");
            }
        } else {
            throw new IllegalArgumentException("Phải cung cấp ảnh (file hoặc URL)");
        }
    }

    private String resolveImageUrl(MultipartFile file, String url) throws IOException {
        return (file != null && !file.isEmpty()) ? cloudinaryService.uploadImage(file) : url;
    }

    private void validatePriceRange(Double min, Double max) {
        if (min != null && min < 0) throw new IllegalArgumentException("Giá min không âm");
        if (max != null && max < 0) throw new IllegalArgumentException("Giá max không âm");
        if (min != null && max != null && min > max) throw new IllegalArgumentException("Giá min không được lớn hơn giá max");
    }

    private Tour toEntity(TourDTO dto) {
        Tour tour = new Tour();
        tour.setName(dto.getName());
        tour.setDuration(dto.getDuration());
        tour.setPrice(dto.getPrice());
        tour.setImageUrl(dto.getImageUrl());
        tour.setDescription(dto.getDescription());
        tour.setMaxParticipants(dto.getMaxParticipants());
        tour.setStatus(dto.getStatus() != null ? dto.getStatus() : TourStatus.ACTIVE);
        return tour;
    }

    private void updateEntity(Tour tour, TourDTO dto, Destination dest, TourCategory category) {
        tour.setName(dto.getName());
        tour.setDuration(dto.getDuration());
        tour.setPrice(dto.getPrice());
        tour.setDescription(dto.getDescription());
        tour.setMaxParticipants(dto.getMaxParticipants());
        tour.setDestination(dest);
        tour.setCategory(category);
        if (dto.getStatus() != null) {
            tour.setStatus(dto.getStatus());
        }
    }
}