// src/main/java/com/travel/travelbooking/service/HotelServiceImpl.java

package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.HotelDTO;
import com.travel.travelbooking.entity.Hotel;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public HotelDTO createHotel(HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException {
        boolean exists = hotelRepository.findAll().stream()
                .anyMatch(h -> h.getName().equalsIgnoreCase(dto.getName().trim()));
        if (exists) {
            throw new IllegalArgumentException("Khách sạn '" + dto.getName() + "' đã tồn tại");
        }

        Hotel hotel = new Hotel();
        hotel.setName(dto.getName().trim());
        hotel.setDescription(dto.getDescription());
        hotel.setAddress(dto.getAddress());
        hotel.setStarRating(dto.getStarRating());
        hotel.setStatus(Hotel.HotelStatus.valueOf(dto.getStatus().toUpperCase()));

        uploadImages(images, hotel.getImages());
        uploadVideos(videos, hotel.getVideos());

        return mapToDTO(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public HotelDTO updateHotel(Long id, HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));

        boolean nameExists = hotelRepository.findAll().stream()
                .anyMatch(h -> !h.getId().equals(id) && h.getName().equalsIgnoreCase(dto.getName().trim()));
        if (nameExists) {
            throw new IllegalArgumentException("Tên khách sạn '" + dto.getName() + "' đã được sử dụng");
        }

        hotel.setName(dto.getName().trim());
        hotel.setDescription(dto.getDescription());
        hotel.setAddress(dto.getAddress());
        hotel.setStarRating(dto.getStarRating());
        hotel.setStatus(Hotel.HotelStatus.valueOf(dto.getStatus().toUpperCase()));

        uploadImages(images, hotel.getImages());
        uploadVideos(videos, hotel.getVideos());

        return mapToDTO(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khách sạn không tồn tại");
        }
        hotelRepository.deleteById(id);
    }

    // XÓA ẢNH RIÊNG LẺ (chỉ xóa URL trong DB)
    @Override
    @Transactional
    public void deleteHotelImage(Long hotelId, String imageUrl) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));

        if (!hotel.getImages().remove(imageUrl)) {
            throw new IllegalArgumentException("Ảnh không tồn tại trong khách sạn này");
        }

        hotelRepository.save(hotel);
    }

    // XÓA VIDEO RIÊNG LẺ (chỉ xóa URL trong DB)
    @Override
    @Transactional
    public void deleteHotelVideo(Long hotelId, String videoUrl) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));

        if (!hotel.getVideos().remove(videoUrl)) {
            throw new IllegalArgumentException("Video không tồn tại trong khách sạn này");
        }

        hotelRepository.save(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelDTO> searchHotels(Pageable pageable, String name, String address, String status, Integer starRating) {
        Hotel.HotelStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Hotel.HotelStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
            }
        }

        Page<Hotel> page = hotelRepository.searchHotels(name, address, statusEnum, starRating, pageable);
        return page.map(this::mapToDTO);
    }

    @Override
    public HotelStats getHotelStats() {
        long total = hotelRepository.count();
        long active = hotelRepository.countByStatus(Hotel.HotelStatus.ACTIVE);
        long fiveStar = hotelRepository.countByStarRating(5);
        long uniqueAddresses = hotelRepository.findAll().stream()
                .map(Hotel::getAddress)
                .map(addr -> addr.split(",")[0].trim())
                .distinct()
                .count();

        return new HotelStats(total, active, fiveStar, uniqueAddresses);
    }

    // ====================== HELPER METHODS ======================

    private void uploadImages(List<MultipartFile> files, List<String> targetList) throws IOException {
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadImage(file);
                    targetList.add(url);
                }
            }
        }
    }

    private void uploadVideos(List<MultipartFile> files, List<String> targetList) throws IOException {
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadVideo(file);
                    targetList.add(url);
                }
            }
        }
    }

    private HotelDTO mapToDTO(Hotel hotel) {
        HotelDTO dto = new HotelDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        dto.setAddress(hotel.getAddress());
        dto.setStarRating(hotel.getStarRating());
        dto.setStatus(hotel.getStatus().name());
        dto.setImages(new ArrayList<>(hotel.getImages()));
        dto.setVideos(new ArrayList<>(hotel.getVideos()));
        return dto;
    }
}