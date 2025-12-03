package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.TourDetailDTO;
import com.travel.travelbooking.dto.TransportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TourDetailService {

    TourDetailDTO createOrUpdateTourDetail(
            Long tourId,
            String itinerary,
            String departurePoint,
            String departureTime,
            String suitableFor,
            String cancellationPolicy,
            List<TransportDTO> transports,
            List<Long> selectedHotelIds,
            List<MultipartFile> additionalImages,
            List<MultipartFile> videos) throws IOException;

    TourDetailDTO getTourDetailDTO(Long tourId);

    void deleteAdditionalImage(Long tourId, String imageUrl);
    void deleteVideo(Long tourId, String videoUrl);
}