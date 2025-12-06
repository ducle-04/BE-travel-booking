package com.travel.travelbooking.repository;

import com.travel.travelbooking.dto.DestinationDTO;
import com.travel.travelbooking.dto.PopularDestinationDTO;
import com.travel.travelbooking.entity.Destination;
import com.travel.travelbooking.entity.DestinationStatus;
import com.travel.travelbooking.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByNameContainingIgnoreCase(String name);
    List<Destination> findByStatus(DestinationStatus status);
    List<Destination> findByRegion(Region region);

    // Lấy danh sách Destination kèm số lượng tour đang hoạt động
    @Query("""
        SELECT new com.travel.travelbooking.dto.DestinationDTO(
            d.id, d.name, d.description, d.imageUrl, d.status, d.region, COUNT(t)
        )
        FROM Destination d
        LEFT JOIN d.tours t ON t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
        GROUP BY d.id, d.name, d.description, d.imageUrl, d.status, d.region
        ORDER BY d.id ASC
    """)
    List<DestinationDTO> findAllWithTourCount();

    // Lấy Destination theo ID kèm số lượng tour đang hoạt động
    @Query("""
        SELECT new com.travel.travelbooking.dto.DestinationDTO(
            d.id, d.name, d.description, d.imageUrl, d.status, d.region, COUNT(t)
        )
        FROM Destination d
        LEFT JOIN d.tours t ON t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
        WHERE d.id = :id
        GROUP BY d.id, d.name, d.description, d.imageUrl, d.status, d.region
    """)
    Optional<DestinationDTO> findByIdWithTourCount(Long id);

    // Tìm kiếm Destination theo tên kèm số lượng tour đang hoạt động
    @Query("""
        SELECT new com.travel.travelbooking.dto.DestinationDTO(
            d.id, d.name, d.description, d.imageUrl, d.status, d.region, COUNT(t)
        )
        FROM Destination d
        LEFT JOIN d.tours t ON t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))
        GROUP BY d.id, d.name, d.description, d.imageUrl, d.status, d.region
        ORDER BY d.id ASC
    """)
    List<DestinationDTO> findByNameContainingIgnoreCaseWithTourCount(String name);

    // Lọc khu vực kèm số lượng tour đang hoạt động
    @Query("""
    SELECT new com.travel.travelbooking.dto.DestinationDTO(
        d.id, d.name, d.description, d.imageUrl, d.status, d.region, COUNT(t)
    )
    FROM Destination d
    LEFT JOIN d.tours t ON t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
    WHERE d.region = :region
      AND d.status != com.travel.travelbooking.entity.DestinationStatus.DELETED
    GROUP BY d.id, d.name, d.description, d.imageUrl, d.status, d.region
    ORDER BY d.id ASC
    """)
    List<DestinationDTO> findByRegionWithTourCount(Region region);

    Optional<Destination> findByName(String name);

    // THỐNG KÊ SỐ LƯỢNG ĐIỂM ĐẾN THEO KHU VỰC
    @Query("""
    SELECT d.region, COUNT(d)
    FROM Destination d
    WHERE d.status = com.travel.travelbooking.entity.DestinationStatus.ACTIVE
    GROUP BY d.region
    ORDER BY COUNT(d) DESC
    """)
    List<Object[]> countDestinationsByRegion();


    // TOP 5 ĐIỂM ĐẾN PHỔ BIẾN NHẤT (dựa trên số tour active + booking + views)
    @Query("""
    SELECT new com.travel.travelbooking.dto.PopularDestinationDTO(
        d.id,
        d.name,
        d.imageUrl,
        d.region,
        COUNT(DISTINCT t.id),
        COALESCE(SUM(t.views), 0),
        COUNT(DISTINCT b.id)
    )
    FROM Destination d
    LEFT JOIN d.tours t ON t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
    LEFT JOIN t.bookings b ON b.status = com.travel.travelbooking.entity.BookingStatus.CONFIRMED
    WHERE d.status = com.travel.travelbooking.entity.DestinationStatus.ACTIVE
    GROUP BY d.id, d.name, d.imageUrl, d.region
    ORDER BY 
        COUNT(DISTINCT t.id) * 0.4 +
        COALESCE(SUM(t.views), 0) * 0.00001 +
        COUNT(DISTINCT b.id) * 0.6 DESC
    """)
    Page<PopularDestinationDTO> findTopPopularDestinations(Pageable pageable);

    default List<PopularDestinationDTO> findTop5PopularDestinations() {
        return findTopPopularDestinations(PageRequest.of(0, 5)).getContent();
    }
}