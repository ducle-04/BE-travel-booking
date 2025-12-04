// src/main/java/com/travel/travelbooking/service/HotelService.java
package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.HotelDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface HotelService {
    HotelDTO createHotel(HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException;
    HotelDTO updateHotel(Long id, HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException;
    void deleteHotel(Long id);
    void deleteHotelImage(Long hotelId, String imageUrl);
    void deleteHotelVideo(Long hotelId, String videoUrl);
    Page<HotelDTO> searchHotels(Pageable pageable, String name, String address, String status, Integer starRating);
    HotelStats getHotelStats(); // ← vẫn giữ nguyên
}