package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TourService {

    // 1. Tạo tour mới
    TourDTO createTour(TourDTO dto, MultipartFile imageFile) throws IOException;

    // 2. Lấy tất cả tour
    java.util.List<TourDTO> getAllTours();

    // 3. Lấy tour theo ID
    TourDTO getTourById(Long id);

    // 4. Tìm kiếm tour theo tên (phân trang)
    Page<TourDTO> searchToursByName(String name, int page);

    // 5. Lấy tour theo điểm đến
    java.util.List<TourDTO> getToursByDestination(Long destinationId);

    // 6. Cập nhật tour
    TourDTO updateTour(Long id, TourDTO dto, MultipartFile imageFile) throws IOException;

    // 7. Xóa mềm tour
    void deleteTour(Long id);

    // 8. Lọc tour nâng cao (phân trang)
    Page<TourDTO> getFilteredTours(String destinationName, TourStatus status,
                                   Double minPrice, Double maxPrice, int page);

    // 9. Thống kê tour
    TourStatsDTO getTourStats();
}