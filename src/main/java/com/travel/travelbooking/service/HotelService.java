package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.HotelDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface HotelService {
    HotelDTO createHotel(HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException;
    HotelDTO updateHotel(Long id, HotelDTO dto, List<MultipartFile> images, List<MultipartFile> videos) throws IOException;
    void deleteHotel(Long id);
    List<HotelDTO> getAllHotels();
    Page<HotelDTO> searchHotels(String name, int page);
    HotelDTO getHotelById(Long id);
}