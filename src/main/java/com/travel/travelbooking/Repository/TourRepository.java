package com.travel.travelbooking.Repository;

import com.travel.travelbooking.Dto.TourDTO;
import com.travel.travelbooking.Dto.TourStatsDTO;
import com.travel.travelbooking.Entity.Destination;
import com.travel.travelbooking.Entity.Tour;
import com.travel.travelbooking.Entity.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByNameContainingIgnoreCase(String name);
    List<Tour> findByStatus(TourStatus status);
    List<Tour> findByDestination(Destination destination);

    @Query("""
        SELECT new com.travel.travelbooking.Dto.TourDTO(
            t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
            t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status, 
            t.createdAt, COUNT(b), COUNT(r), t.maxParticipants
        )
        FROM Tour t
        LEFT JOIN t.destination d
        LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
        LEFT JOIN t.reviews r
        WHERE t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
        GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
                 t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt, t.maxParticipants
        ORDER BY t.id ASC
        """)
    List<TourDTO> findAllWithCounts();

    @Query("""
        SELECT new com.travel.travelbooking.Dto.TourDTO(
            t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
            t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status, 
            t.createdAt, COUNT(b), COUNT(r), t.maxParticipants
        )
        FROM Tour t
        LEFT JOIN t.destination d
        LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
        LEFT JOIN t.reviews r
        WHERE t.id = :id AND t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
        GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
                 t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt, t.maxParticipants
        """)
    Optional<TourDTO> findByIdWithCounts(Long id);

    @Query("""
        SELECT new com.travel.travelbooking.Dto.TourDTO(
            t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
            t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status, 
            t.createdAt, COUNT(b), COUNT(r), t.maxParticipants
        )
        FROM Tour t
        LEFT JOIN t.destination d
        LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
        LEFT JOIN t.reviews r
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) 
        AND t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
        GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
                 t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt, t.maxParticipants
        ORDER BY t.id ASC
        """)
    List<TourDTO> findByNameContainingIgnoreCaseWithCounts(String name);

    @Query("""
        SELECT new com.travel.travelbooking.Dto.TourDTO(
            t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
            t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status, 
            t.createdAt, COUNT(b), COUNT(r), t.maxParticipants
        )
        FROM Tour t
        LEFT JOIN t.destination d
        LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
        LEFT JOIN t.reviews r
        WHERE t.destination = :destination AND t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
        GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
                 t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt, t.maxParticipants
        ORDER BY t.id ASC
        """)
    List<TourDTO> findByDestinationWithCounts(Destination destination);

    @Query("""
        SELECT new com.travel.travelbooking.Dto.TourDTO(
            t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
            t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status, 
            t.createdAt, COUNT(b), COUNT(r), t.maxParticipants
        )
        FROM Tour t
        LEFT JOIN t.destination d
        LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
        LEFT JOIN t.reviews r
        WHERE (:destinationName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :destinationName, '%')))
        AND (:status IS NULL OR t.status = :status)
        AND (:minPrice IS NULL OR t.price >= :minPrice)
        AND (:maxPrice IS NULL OR t.price <= :maxPrice)
        AND t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
        GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
                 t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt, t.maxParticipants
        ORDER BY t.id ASC
        """)
    Page<TourDTO> findFilteredTours(String destinationName, TourStatus status, Double minPrice, Double maxPrice, Pageable pageable);

    @Query("""
    SELECT new com.travel.travelbooking.Dto.TourStatsDTO(
        COUNT(DISTINCT t.id),
        COUNT(DISTINCT CASE WHEN t.status = 'ACTIVE' THEN t.id END),
        COUNT(DISTINCT CASE WHEN t.status = 'INACTIVE' THEN t.id END),
        COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0)
    )
    FROM Tour t
    LEFT JOIN t.bookings b
    WHERE t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
    """)
    TourStatsDTO getTourStats();

    @Query("""
        SELECT new com.travel.travelbooking.Dto.TourDTO(
            t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
            t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status, 
            t.createdAt, COUNT(b), COUNT(r), t.maxParticipants
        )
        FROM Tour t
        LEFT JOIN t.destination d
        LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
        LEFT JOIN t.reviews r
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) 
        AND t.status != com.travel.travelbooking.Entity.TourStatus.DELETED
        GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl, 
                 t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt, t.maxParticipants
        ORDER BY t.id ASC
        """)
    Page<TourDTO> findByNameContainingIgnoreCaseWithCountsAndPageable(String name, Pageable pageable);
}