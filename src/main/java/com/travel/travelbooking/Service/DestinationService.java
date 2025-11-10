package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.DestinationDTO;
import com.travel.travelbooking.Entity.*;
import com.travel.travelbooking.Exception.ResourceNotFoundException;
import com.travel.travelbooking.Repository.DestinationRepository;
import com.travel.travelbooking.Repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final TourRepository tourRepository;
    private final CloudinaryService cloudinaryService;

    private Destination toEntity(DestinationDTO dto, Destination dest) {
        dest.setName(dto.getName());
        dest.setDescription(dto.getDescription());
        dest.setRegion(dto.getRegion());
        dest.setStatus(dto.getStatus() != null ? dto.getStatus() : DestinationStatus.ACTIVE);
        return dest;
    }

    private DestinationDTO findWithTourCount(Long id) {
        return destinationRepository.findByIdWithTourCount(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không thể lấy thông tin điểm đến"));
    }

    // === CREATE ===
    @Transactional
    public DestinationDTO createDestination(DestinationDTO dto, MultipartFile imageFile) throws IOException {
        validateImage(imageFile, dto.getImageUrl());
        if (isNameExists(dto.getName(), null)) {
            throw new IllegalArgumentException("Tên điểm đến đã tồn tại");
        }

        String imageUrl = resolveImageUrl(imageFile, dto.getImageUrl());
        dto.setImageUrl(imageUrl);

        Destination dest = destinationRepository.save(toEntity(dto, new Destination()));
        return findWithTourCount(dest.getId());
    }

    // === READ ===
    public List<DestinationDTO> getAllDestinations() {
        return destinationRepository.findAllWithTourCount();
    }

    public DestinationDTO getDestinationById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID không hợp lệ");
        return findWithTourCount(id);
    }

    public List<DestinationDTO> searchDestinationsByName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Tên tìm kiếm trống");
        return destinationRepository.findByNameContainingIgnoreCaseWithTourCount(name);
    }

    public List<DestinationDTO> getDestinationsByRegion(Region region) {
        return destinationRepository.findByRegion(region).stream()
                .filter(d -> d.getStatus() != DestinationStatus.DELETED)
                .map(d -> new DestinationDTO(d.getId(), d.getName(), d.getDescription(),
                        d.getImageUrl(), d.getStatus(), d.getRegion()))
                .toList();
    }

    // === UPDATE ===
    @Transactional
    public DestinationDTO updateDestination(Long id, DestinationDTO dto, MultipartFile imageFile) throws IOException {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID không hợp lệ");

        Destination dest = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Điểm đến không tồn tại"));

        if (!dest.getName().equalsIgnoreCase(dto.getName()) && isNameExists(dto.getName(), id)) {
            throw new IllegalArgumentException("Tên điểm đến đã tồn tại");
        }

        validateImage(imageFile, dto.getImageUrl());
        String imageUrl = resolveImageUrl(imageFile, dto.getImageUrl());
        if (imageUrl != null) dest.setImageUrl(imageUrl);

        toEntity(dto, dest);
        syncTourStatus(dest);

        destinationRepository.save(dest);
        return findWithTourCount(id);
    }

    // === DELETE ===
    @Transactional
    public void deleteDestination(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID không hợp lệ");

        Destination dest = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Điểm đến không tồn tại"));

        dest.setStatus(DestinationStatus.DELETED);
        dest.getTours().forEach(t -> t.setStatus(TourStatus.DELETED));
        tourRepository.saveAll(dest.getTours());
        destinationRepository.save(dest);
    }

    // === Helpers ===
    private void validateImage(MultipartFile file, String url) throws IOException {
        if (file != null && !file.isEmpty()) {
            if (!file.getContentType().startsWith("image/"))
                throw new IllegalArgumentException("File phải là ảnh");
            if (file.getSize() > 5 * 1024 * 1024)
                throw new IllegalArgumentException("Ảnh không quá 5MB");
        } else if (url != null && !url.trim().isEmpty()) {
            try {
                new URL(url).toURI();
            } catch (Exception e) {
                throw new IllegalArgumentException("URL ảnh không hợp lệ");
            }
        }
    }

    private String resolveImageUrl(MultipartFile file, String url) throws IOException {
        return (file != null && !file.isEmpty()) ? cloudinaryService.uploadImage(file) : url;
    }

    private boolean isNameExists(String name, Long excludeId) {
        return destinationRepository.findByNameContainingIgnoreCase(name).stream()
                .anyMatch(d -> excludeId == null || !d.getId().equals(excludeId));
    }

    private void syncTourStatus(Destination dest) {
        if (dest.getStatus() == DestinationStatus.INACTIVE) {
            dest.getTours().forEach(t -> t.setStatus(TourStatus.INACTIVE));
            tourRepository.saveAll(dest.getTours());
        }
    }
}