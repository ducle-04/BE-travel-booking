package com.travel.travelbooking.repository;

import com.travel.travelbooking.dto.DestinationDTO;
import com.travel.travelbooking.entity.Destination;
import com.travel.travelbooking.entity.DestinationStatus;
import com.travel.travelbooking.entity.Region;
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
}