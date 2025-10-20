package com.travel.travelbooking.Repository;

import com.travel.travelbooking.Entity.Tour;
import com.travel.travelbooking.Entity.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    // Tìm tour theo tên (không phân biệt hoa thường)
    List<Tour> findByNameContainingIgnoreCase(String name);

    // Tìm tour theo điểm đến
    List<Tour> findByDestinationId(Long destinationId);

    // Tìm tour theo trạng thái
    List<Tour> findByStatus(TourStatus status);
}