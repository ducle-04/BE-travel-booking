package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.HotelDTO;
import com.travel.travelbooking.dto.TourDetailDTO;
import com.travel.travelbooking.dto.TransportDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.HotelRepository;
import com.travel.travelbooking.repository.TourDetailRepository;
import com.travel.travelbooking.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourDetailServiceImpl implements TourDetailService {

    private final TourDetailRepository tourDetailRepository;
    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;
    private final CloudinaryService cloudinaryService;

    // CREATE / UPDATE → TRẢ DTO NGAY TRONG @Transactional
    @Override
    @Transactional
    public TourDetailDTO createOrUpdateTourDetail(
            Long tourId,
            String itinerary,
            String departurePoint,
            String departureTime,
            String suitableFor,
            String cancellationPolicy,
            List<TransportDTO> transports,
            List<Long> selectedHotelIds,
            List<MultipartFile> additionalImages,
            List<MultipartFile> videos) throws IOException {

        // 1. Tìm tour
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại với ID: " + tourId));

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
        tourDetail.setItinerary(itinerary);
        tourDetail.setDeparturePoint(departurePoint);
        tourDetail.setDepartureTime(departureTime);
        tourDetail.setSuitableFor(suitableFor);
        tourDetail.setCancellationPolicy(cancellationPolicy);

        // 4. Cập nhật phương tiện
        tourDetail.getTransports().clear();
        if (transports != null && !transports.isEmpty()) {
            for (TransportDTO dto : transports) {
                Transport transport = new Transport();
                transport.setName(dto.getName());
                transport.setPrice(dto.getPrice());
                tourDetail.getTransports().add(transport);
            }
        }

        // 5. Cập nhật khách sạn được chọn
        tourDetail.getSelectedHotelIds().clear();
        if (selectedHotelIds != null && !selectedHotelIds.isEmpty()) {
            // Kiểm tra tồn tại
            List<Hotel> hotels = hotelRepository.findAllById(selectedHotelIds);
            if (hotels.size() != selectedHotelIds.size()) {
                throw new IllegalArgumentException("Một số khách sạn không tồn tại");
            }
            tourDetail.getSelectedHotelIds().addAll(selectedHotelIds);
        }

        // 6. Upload và thêm ảnh mới
        if (additionalImages != null && !additionalImages.isEmpty()) {
            for (MultipartFile file : additionalImages) {
                if (file != null && !file.isEmpty()) {
                    String url = cloudinaryService.uploadImage(file);
                    tourDetail.getAdditionalImages().add(url);
                }
            }
        }

        // 7. Upload và thêm video mới
        if (videos != null && !videos.isEmpty()) {
            for (MultipartFile file : videos) {
                if (file != null && !file.isEmpty()) {
                    String url = cloudinaryService.uploadVideo(file);
                    tourDetail.getVideos().add(url);
                }
            }
        }

        // 8. Lưu
        TourDetail savedDetail = tourDetailRepository.save(tourDetail);

        if (isNew) {
            tour.setDetail(savedDetail);
            tourRepository.save(tour);
        }

        // 9. Trả DTO + load thông tin khách sạn đầy đủ
        TourDetailDTO dto = mapToDTO(savedDetail);
        enhanceWithHotels(savedDetail, dto);
        return dto;
    }

    // GET DETAIL → TRẢ DTO + HOTELS
    @Override
    @Transactional(readOnly = true)
    public TourDetailDTO getTourDetailDTO(Long tourId) {
        TourDetail detail = tourDetailRepository.findByTourId(tourId).orElse(null);
        if (detail == null) return null;

        TourDetailDTO dto = mapToDTO(detail);
        enhanceWithHotels(detail, dto);
        return dto;
    }

    // DELETE IMAGE
    @Override
    @Transactional
    public void deleteAdditionalImage(Long tourId, String imageUrl) {
        TourDetail detail = tourDetailRepository.findByTourId(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Chi tiết tour không tồn tại"));
        detail.getAdditionalImages().remove(imageUrl);
        tourDetailRepository.save(detail);
    }

    // DELETE VIDEO
    @Override
    @Transactional
    public void deleteVideo(Long tourId, String videoUrl) {
        TourDetail detail = tourDetailRepository.findByTourId(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Chi tiết tour không tồn tại"));
        detail.getVideos().remove(videoUrl);
        tourDetailRepository.save(detail);
    }

    // === PRIVATE HELPER METHODS ===

    private void enhanceWithHotels(TourDetail detail, TourDetailDTO dto) {
        if (detail.getSelectedHotelIds() != null && !detail.getSelectedHotelIds().isEmpty()) {
            List<Hotel> hotels = hotelRepository.findAllById(detail.getSelectedHotelIds());
            dto.setSelectedHotels(hotels.stream().map(this::mapHotelToDTO).toList());
        }
    }

    private HotelDTO mapHotelToDTO(Hotel hotel) {
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

    private TourDetailDTO mapToDTO(TourDetail detail) {
        TourDetailDTO dto = new TourDetailDTO();
        dto.setItinerary(detail.getItinerary());
        dto.setDeparturePoint(detail.getDeparturePoint());
        dto.setDepartureTime(detail.getDepartureTime());
        dto.setSuitableFor(detail.getSuitableFor());
        dto.setCancellationPolicy(detail.getCancellationPolicy());

        // Map transports
        dto.setTransports(detail.getTransports().stream().map(t -> {
            TransportDTO td = new TransportDTO();
            td.setName(t.getName());
            td.setPrice(t.getPrice());
            return td;
        }).toList());

        // Copy media
        dto.setAdditionalImages(new ArrayList<>(detail.getAdditionalImages()));
        dto.setVideos(new ArrayList<>(detail.getVideos()));
        return dto;
    }
}