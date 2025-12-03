package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.HotelDTO;
import com.travel.travelbooking.entity.Hotel;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        if (hotelRepository.findByNameIgnoreCase(dto.getName().trim()).isPresent()) {
            throw new IllegalArgumentException("Khách sạn '" + dto.getName() + "' đã tồn tại");
        }

        Hotel hotel = new Hotel();
        hotel.setName(dto.getName().trim());
        hotel.setDescription(dto.getDescription());
        hotel.setAddress(dto.getAddress());
        hotel.setStarRating(dto.getStarRating());

        uploadMedia(images, hotel.getImages(), cloudinaryService::uploadImage);
        uploadMedia(videos, hotel.getVideos(), cloudinaryService::uploadVideo);

        Hotel saved = hotelRepository.save(hotel);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public HotelDTO updateHotel(Long id, HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));

        hotel.setDescription(dto.getDescription());
        hotel.setAddress(dto.getAddress());
        hotel.setStarRating(dto.getStarRating());

        uploadMedia(images, hotel.getImages(), cloudinaryService::uploadImage);
        uploadMedia(videos, hotel.getVideos(), cloudinaryService::uploadVideo);

        Hotel saved = hotelRepository.save(hotel);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khách sạn không tồn tại");
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDTO> getAllHotels() {
        return hotelRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelDTO> searchHotels(String name, int page) {
        PageRequest pr = PageRequest.of(page, 10);
        Page<Hotel> hotelPage = hotelRepository.findByNameContainingIgnoreCase(name, pr);
        return hotelPage.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDTO getHotelById(Long id) {
        return hotelRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));
    }

    private <T> void uploadMedia(List<MultipartFile> files, List<T> list, MediaUploader<T> uploader) throws IOException {
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    list.add(uploader.upload(file));
                }
            }
        }
    }

    @FunctionalInterface
    interface MediaUploader<T> {
        T upload(MultipartFile file) throws IOException;
    }

    private HotelDTO mapToDTO(Hotel hotel) {
        HotelDTO dto = new HotelDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        dto.setAddress(hotel.getAddress());
        dto.setStarRating(hotel.getStarRating());
        dto.setImages(new ArrayList<>(hotel.getImages()));
        dto.setVideos(new ArrayList<>(hotel.getVideos()));
        return dto;
    }
}