package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.DestinationDTO;
import com.travel.travelbooking.Entity.*;
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
public class DestinationService {

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private DestinationDTO toDTO(Destination destination) {
        DestinationDTO dto = new DestinationDTO();
        dto.setId(destination.getId());
        dto.setName(destination.getName());
        dto.setDescription(destination.getDescription());
        dto.setImageUrl(destination.getImageUrl());
        dto.setStatus(destination.getStatus());
        dto.setRegion(destination.getRegion());
        return dto;
    }

    private Destination toEntity(DestinationDTO dto) {
        Destination destination = new Destination();
        destination.setId(dto.getId());
        destination.setName(dto.getName());
        destination.setDescription(dto.getDescription());
        destination.setImageUrl(dto.getImageUrl());
        destination.setStatus(dto.getStatus() != null ? dto.getStatus() : DestinationStatus.ACTIVE);
        destination.setRegion(dto.getRegion());
        return destination;
    }

    public DestinationDTO createDestination(DestinationDTO destinationDTO, MultipartFile imageFile) throws IOException {
        if (destinationDTO.getName() == null || destinationDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên điểm đến không được để trống");
        }
        if (destinationDTO.getRegion() == null) {
            throw new IllegalArgumentException("Khu vực (region) không được để trống");
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile);
            destinationDTO.setImageUrl(imageUrl);
        } else if (destinationDTO.getImageUrl() != null && !destinationDTO.getImageUrl().trim().isEmpty()) {
            try {
                new URL(destinationDTO.getImageUrl()).toURI();
            } catch (Exception e) {
                throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
            }
        }
        if (destinationRepository.findByNameContainingIgnoreCase(destinationDTO.getName()).stream()
                .anyMatch(dest -> !dest.getId().equals(destinationDTO.getId()))) {
            throw new IllegalArgumentException("Điểm đến với tên '" + destinationDTO.getName() + "' đã tồn tại");
        }

        Destination destination = toEntity(destinationDTO);
        Destination savedDestination = destinationRepository.save(destination);
        return destinationRepository.findByIdWithTourCount(savedDestination.getId())
                .orElseThrow(() -> new RuntimeException("Lỗi khi lấy thông tin điểm đến vừa tạo"));
    }

    public List<DestinationDTO> getAllDestinations() {
        return destinationRepository.findAllWithTourCount();
    }

    public DestinationDTO getDestinationById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID điểm đến không hợp lệ");
        }
        return destinationRepository.findByIdWithTourCount(id)
                .orElseThrow(() -> new RuntimeException("Điểm đến không tồn tại với ID: " + id));
    }

    public List<DestinationDTO> searchDestinationsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên điểm đến không được để trống");
        }
        return destinationRepository.findByNameContainingIgnoreCaseWithTourCount(name);
    }

    public DestinationDTO updateDestination(Long id, DestinationDTO destinationDTO, MultipartFile imageFile) throws IOException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID điểm đến không hợp lệ");
        }
        if (destinationDTO.getName() == null || destinationDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên điểm đến không được để trống");
        }
        if (destinationDTO.getRegion() == null) {
            throw new IllegalArgumentException("Khu vực (region) không được để trống");
        }

        Optional<Destination> existingDestination = destinationRepository.findById(id);
        if (existingDestination.isPresent()) {
            Destination destination = existingDestination.get();
            if (!destination.getName().equals(destinationDTO.getName()) &&
                    destinationRepository.findByNameContainingIgnoreCase(destinationDTO.getName()).stream()
                            .anyMatch(d -> !d.getId().equals(id))) {
                throw new IllegalArgumentException("Điểm đến với tên '" + destinationDTO.getName() + "' đã tồn tại");
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String newImageUrl = cloudinaryService.uploadImage(imageFile);
                destination.setImageUrl(newImageUrl);
            } else if (destinationDTO.getImageUrl() != null && !destinationDTO.getImageUrl().trim().isEmpty()) {
                try {
                    new URL(destinationDTO.getImageUrl()).toURI();
                    destination.setImageUrl(destinationDTO.getImageUrl());
                } catch (Exception e) {
                    throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
                }
            }
            destination.setName(destinationDTO.getName());
            destination.setDescription(destinationDTO.getDescription());
            destination.setStatus(destinationDTO.getStatus() != null ? destinationDTO.getStatus() : DestinationStatus.ACTIVE);
            destination.setRegion(destinationDTO.getRegion());
            Destination updatedDestination = destinationRepository.save(destination);
            return destinationRepository.findByIdWithTourCount(id)
                    .orElseThrow(() -> new RuntimeException("Lỗi khi lấy thông tin điểm đến vừa cập nhật"));
        } else {
            throw new RuntimeException("Điểm đến không tồn tại với ID: " + id);
        }
    }

    public List<DestinationDTO> getDestinationsByRegion(Region region) {
        return destinationRepository.findByRegion(region).stream()
                .filter(dest -> dest.getStatus() != DestinationStatus.DELETED)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDestination(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID điểm đến không hợp lệ");
        }
        Optional<Destination> destinationOptional = destinationRepository.findById(id);
        if (destinationOptional.isPresent()) {
            Destination destination = destinationOptional.get();
            destination.setStatus(DestinationStatus.DELETED);
            List<Tour> tours = destination.getTours();
            for (Tour tour : tours) {
                tour.setStatus(TourStatus.INACTIVE);
            }
            tourRepository.saveAll(tours);
            destinationRepository.save(destination);
        } else {
            throw new RuntimeException("Điểm đến không tồn tại với ID: " + id);
        }
    }
}