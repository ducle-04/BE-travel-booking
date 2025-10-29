// src/main/java/com/travel/travelbooking/Service/TourDetailService.java
package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.TourDetailDTO;
import com.travel.travelbooking.Entity.Tour;
import com.travel.travelbooking.Entity.TourDetail;
import com.travel.travelbooking.Entity.TourStatus;
import com.travel.travelbooking.Repository.TourDetailRepository;
import com.travel.travelbooking.Repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TourDetailService {

    @Autowired
    private TourDetailRepository tourDetailRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // CREATE / UPDATE → TRẢ DTO NGAY TRONG @Transactional
    @Transactional
    public TourDetailDTO createOrUpdateTourDetail(
            Long tourId,
            String transportation,
            String itinerary,
            String departurePoint,
            String departureTime,
            String suitableFor,
            String cancellationPolicy,
            List<MultipartFile> additionalImages,
            List<MultipartFile> videos) throws IOException {

        // 1. Tìm tour
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour không tồn tại với ID: " + tourId));

        if (tour.getStatus() == TourStatus.DELETED) {
            throw new IllegalArgumentException("Không thể thêm chi tiết cho tour đã bị xóa");
        }

        // 2. Lấy hoặc tạo mới TourDetail
        TourDetail tourDetail = tour.getDetail();
        boolean isNew = tourDetail == null;
        if (isNew) {
            tourDetail = new TourDetail();
            tourDetail.setTour(tour);
        }

        // 3. Cập nhật text
        tourDetail.setTransportation(transportation);
        tourDetail.setItinerary(itinerary);
        tourDetail.setDeparturePoint(departurePoint);
        tourDetail.setDepartureTime(departureTime);
        tourDetail.setSuitableFor(suitableFor);
        tourDetail.setCancellationPolicy(cancellationPolicy);

        // 4. Upload và thêm ảnh mới
        if (additionalImages != null && !additionalImages.isEmpty()) {
            for (MultipartFile file : additionalImages) {
                if (file != null && !file.isEmpty()) {
                    String url = cloudinaryService.uploadImage(file);
                    tourDetail.getAdditionalImages().add(url);
                }
            }
        }

        // 5. Upload và thêm video mới
        if (videos != null && !videos.isEmpty()) {
            for (MultipartFile file : videos) {
                if (file != null && !file.isEmpty()) {
                    String url = cloudinaryService.uploadVideo(file);
                    tourDetail.getVideos().add(url);
                }
            }
        }

        // 6. Lưu
        TourDetail savedDetail = tourDetailRepository.save(tourDetail);

        if (isNew) {
            tour.setDetail(savedDetail);
            tourRepository.save(tour);
        }

        // TRẢ DTO NGAY TRONG TRANSACTION → AN TOÀN 100%
        return mapToDTO(savedDetail);
    }

    // GET DETAIL → TRẢ DTO
    @Transactional(readOnly = true)
    public TourDetailDTO getTourDetailDTO(Long tourId) {
        TourDetail detail = tourDetailRepository.findByTourId(tourId).orElse(null);
        return detail != null ? mapToDTO(detail) : null;
    }

    // DELETE IMAGE
    @Transactional
    public void deleteAdditionalImage(Long tourId, String imageUrl) {
        TourDetail detail = tourDetailRepository.findByTourId(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Chi tiết tour không tồn tại"));
        detail.getAdditionalImages().remove(imageUrl);
        tourDetailRepository.save(detail);
    }

    // DELETE VIDEO
    @Transactional
    public void deleteVideo(Long tourId, String videoUrl) {
        TourDetail detail = tourDetailRepository.findByTourId(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Chi tiết tour không tồn tại"));
        detail.getVideos().remove(videoUrl);
        tourDetailRepository.save(detail);
    }

    // MAP ENTITY → DTO (AN TOÀN, KHÔNG DÙNG @Transactional)
    private TourDetailDTO mapToDTO(TourDetail detail) {
        TourDetailDTO dto = new TourDetailDTO();
        dto.setTransportation(detail.getTransportation());
        dto.setItinerary(detail.getItinerary());
        dto.setDeparturePoint(detail.getDeparturePoint());
        dto.setDepartureTime(detail.getDepartureTime());
        dto.setSuitableFor(detail.getSuitableFor());
        dto.setCancellationPolicy(detail.getCancellationPolicy());
        dto.setAdditionalImages(new ArrayList<>(detail.getAdditionalImages()));
        dto.setVideos(new ArrayList<>(detail.getVideos()));
        return dto;
    }
}