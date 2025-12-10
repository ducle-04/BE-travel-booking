package com.travel.travelbooking.repository;

import com.travel.travelbooking.dto.*;
import com.travel.travelbooking.entity.Destination;
import com.travel.travelbooking.entity.Tour;
import com.travel.travelbooking.entity.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    List<Tour> findByNameContainingIgnoreCase(String name);
    List<Tour> findByStatus(TourStatus status);
    List<Tour> findByDestination(Destination destination);

    // === TĂNG LƯỢT XEM ===
    @Modifying
    @Transactional
    @Query(value = "UPDATE tours SET views = views + 1 WHERE id = :id AND status <> 'DELETED'", nativeQuery = true)
    void incrementViews(@Param("id") Long id);

    /* ----------------------------------------------------------
        1. Lấy tất cả tour
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price, 
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE t.status <> 'DELETED'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, 
             t.imageUrl, t.description, t.averageRating, 
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.id ASC
    """)
    List<TourDTO> findAllWithCounts();

    /* ----------------------------------------------------------
        2. Lấy tour theo ID
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE t.id = :id AND t.status <> 'DELETED'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    """)
    Optional<TourDTO> findByIdWithCounts(@Param("id") Long id);

    /* ----------------------------------------------------------
        3. Tìm kiếm theo tên
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
      AND t.status <> 'DELETED'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.id ASC
    """)
    List<TourDTO> findByNameContainingIgnoreCaseWithCounts(@Param("name") String name);

    /* ----------------------------------------------------------
        4. Lấy theo destination
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE t.destination = :destination
      AND t.status <> 'DELETED'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.id ASC
    """)
    List<TourDTO> findByDestinationWithCounts(Destination destination);

    /* ----------------------------------------------------------
        5. Lọc nâng cao
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE (:destinationName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :destinationName, '%')))
      AND (:status IS NULL OR t.status = :status)
      AND (:minPrice IS NULL OR t.price >= :minPrice)
      AND (:maxPrice IS NULL OR t.price <= :maxPrice)
      AND (:categoryId IS NULL OR c.id = :categoryId)
      AND t.status <> 'DELETED'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.id ASC
    """)
    Page<TourDTO> findFilteredTours(
            @Param("destinationName") String destinationName,
            @Param("status") TourStatus status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    /* ----------------------------------------------------------
        6. Tìm kiếm theo tên + phân trang
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
      AND t.status <> 'DELETED'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.id ASC
    """)
    Page<TourDTO> findByNameContainingIgnoreCaseWithCountsAndPageable(
            @Param("name") String name,
            Pageable pageable
    );

    /* ----------------------------------------------------------
        7. Thống kê tour
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourStatsDTO(
        COUNT(DISTINCT t.id),
        COUNT(DISTINCT CASE WHEN t.status = 'ACTIVE' THEN t.id END),
        COUNT(DISTINCT CASE WHEN t.status = 'INACTIVE' THEN t.id END),
        COUNT(DISTINCT CASE WHEN b.status = 'CONFIRMED' THEN b.id END)
    )
    FROM Tour t
    LEFT JOIN t.bookings b
    WHERE t.status <> 'DELETED'
    """)
    TourStatsDTO getTourStats();

    /* ----------------------------------------------------------
        8. Lấy theo category không phân trang
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE t.category.id = :categoryId
      AND t.status = 'ACTIVE'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.createdAt DESC
    """)
    List<TourDTO> findByCategoryIdWithCounts(@Param("categoryId") Long categoryId);

    /* ----------------------------------------------------------
        9. Lấy theo category + phân trang
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price,
        t.imageUrl, t.description, t.averageRating,
        t.status, t.createdAt,
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        t.maxParticipants,
        c.id, c.name, c.icon,
        COALESCE(t.views, 0)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE t.category.id = :categoryId
      AND t.status = 'ACTIVE'
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price,
             t.imageUrl, t.description, t.averageRating,
             t.status, t.createdAt, t.maxParticipants,
             c.id, c.name, c.icon, t.views
    ORDER BY t.createdAt DESC
    """)
    Page<TourDTO> findByCategoryIdWithCountsPaged(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    /* ----------------------------------------------------------
        10. Popular Tour
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.PopularTourDTO(
        t.id, t.name, t.imageUrl, d.name, t.description,
        COALESCE(t.views, 0),
        COUNT(DISTINCT b.id),
        COUNT(DISTINCT r.id),
        COALESCE(t.averageRating, 0.0),
        (COALESCE(t.views, 0) * 0.3) 
        + (COUNT(DISTINCT b.id) * 0.5) 
        + (COUNT(DISTINCT r.id) * 0.2)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    LEFT JOIN t.reviews r
    WHERE t.status = 'ACTIVE'
    GROUP BY t.id, t.name, t.imageUrl, d.name, t.description, t.views, t.averageRating
    ORDER BY 
        (COALESCE(t.views, 0) * 0.3) 
        + (COUNT(DISTINCT b.id) * 0.5)
        + (COUNT(DISTINCT r.id) * 0.2) DESC
    """)
    Page<PopularTourDTO> findTopPopularTours(Pageable pageable);

    default List<PopularTourDTO> findTop10PopularTours() {
        return findTopPopularTours(PageRequest.of(0, 10)).getContent();
    }

    /* ----------------------------------------------------------
        11. Top booked tours
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.TopBookedTourDTO(
        t.id, t.name, t.imageUrl, d.name,
        COUNT(b.id)
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.bookings b 
        ON b.status IN ('CONFIRMED','COMPLETED')
    WHERE t.status = 'ACTIVE'
    GROUP BY t.id, t.name, t.imageUrl, d.name
    ORDER BY COUNT(b.id) DESC
    """)
    Page<TopBookedTourDTO> findTopBookedTours(Pageable pageable);

    default List<TopBookedTourDTO> findTop5BookedTours() {
        return findTopBookedTours(PageRequest.of(0, 5)).getContent();
    }

    /* ----------------------------------------------------------
        12. Latest tours
       ---------------------------------------------------------- */
    @Query("""
    SELECT new com.travel.travelbooking.dto.LatestTourDTO(
        t.id, t.name, t.imageUrl, t.description, d.name,
        t.createdAt, t.status
    )
    FROM Tour t
    LEFT JOIN t.destination d
    WHERE t.status <> 'DELETED'
    ORDER BY t.createdAt DESC
    """)
    Page<LatestTourDTO> findLatestTours(Pageable pageable);

    default List<LatestTourDTO> findTop10LatestTours() {
        return findLatestTours(PageRequest.of(0, 10)).getContent();
    }
}
