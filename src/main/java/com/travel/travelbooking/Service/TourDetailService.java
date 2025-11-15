package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.TourDetailDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TourDetailService {

    // CREATE / UPDATE chi tiết tour
    TourDetailDTO createOrUpdateTourDetail(
            Long tourId,
            String transportation,
            String itinerary,
            String departurePoint,
            String departureTime,
            String suitableFor,
            String cancellationPolicy,
            List<MultipartFile> additionalImages,
            List<MultipartFile> videos) throws IOException;

    // GET chi tiết tour
    TourDetailDTO getTourDetailDTO(Long tourId);

    // XÓA ảnh phụ
    void deleteAdditionalImage(Long tourId, String imageUrl);

    // XÓA video
    void deleteVideo(Long tourId, String videoUrl);
}