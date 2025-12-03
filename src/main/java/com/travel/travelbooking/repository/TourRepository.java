package com.travel.travelbooking.repository;

import com.travel.travelbooking.dto.TourDTO;
import com.travel.travelbooking.dto.TourStatsDTO;
import com.travel.travelbooking.entity.Destination;
import com.travel.travelbooking.entity.Tour;
import com.travel.travelbooking.entity.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    List<Tour> findByNameContainingIgnoreCase(String name);
    List<Tour> findByStatus(TourStatus status);
    List<Tour> findByDestination(Destination destination);

    // 1. Lấy tất cả tour + count + category
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourDTO(
         t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
         t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
         t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
         c.id, c.name, c.icon
     )
     FROM Tour t
     LEFT JOIN t.destination d
     LEFT JOIN t.category c
     LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
     LEFT JOIN t.reviews r
     WHERE t.status != com.travel.travelbooking.entity.TourStatus.DELETED
     GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
              t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
              t.maxParticipants, c.id, c.name, c.icon
     ORDER BY t.id ASC
     """)
    List<TourDTO> findAllWithCounts();

    // 2. Lấy tour theo ID + count + category
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourDTO(
         t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
         t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
         t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
         c.id, c.name, c.icon
     )
     FROM Tour t
     LEFT JOIN t.destination d
     LEFT JOIN t.category c
     LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
     LEFT JOIN t.reviews r
     WHERE t.id = :id AND t.status != com.travel.travelbooking.entity.TourStatus.DELETED
     GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
              t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
              t.maxParticipants, c.id, c.name, c.icon
     """)
    Optional<TourDTO> findByIdWithCounts(@Param("id") Long id);

    // 3. Tìm kiếm theo tên (không phân trang)
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourDTO(
         t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
         t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
         t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
         c.id, c.name, c.icon
     )
     FROM Tour t
     LEFT JOIN t.destination d
     LEFT JOIN t.category c
     LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
     LEFT JOIN t.reviews r
     WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
       AND t.status != com.travel.travelbooking.entity.TourStatus.DELETED
     GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
              t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
              t.maxParticipants, c.id, c.name, c.icon
     ORDER BY t.id ASC
     """)
    List<TourDTO> findByNameContainingIgnoreCaseWithCounts(@Param("name") String name);

    // 4. Lấy tour theo destination
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourDTO(
         t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
         t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
         t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
         c.id, c.name, c.icon
     )
     FROM Tour t
     LEFT JOIN t.destination d
     LEFT JOIN t.category c
     LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
     LEFT JOIN t.reviews r
     WHERE t.destination = :destination AND t.status != com.travel.travelbooking.entity.TourStatus.DELETED
     GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
              t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
              t.maxParticipants, c.id, c.name, c.icon
     ORDER BY t.id ASC
     """)
    List<TourDTO> findByDestinationWithCounts(Destination destination);

    // 5. Lọc tour nâng cao + phân trang
    @Query("""
 SELECT new com.travel.travelbooking.dto.TourDTO(
     t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
     t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
     t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
     c.id, c.name, c.icon
 )
 FROM Tour t
 LEFT JOIN t.destination d
 LEFT JOIN t.category c
 LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
 LEFT JOIN t.reviews r
 WHERE (:destinationName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :destinationName, '%')))
   AND (:status IS NULL OR t.status = :status)
   AND (:minPrice IS NULL OR t.price >= :minPrice)
   AND (:maxPrice IS NULL OR t.price <= :maxPrice)
   AND (:categoryId IS NULL OR c.id = :categoryId)  
   AND t.status != com.travel.travelbooking.entity.TourStatus.DELETED
 GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
          t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
          t.maxParticipants, c.id, c.name, c.icon
 ORDER BY t.id ASC
""")
    Page<TourDTO> findFilteredTours(
            @Param("destinationName") String destinationName,
            @Param("status") TourStatus status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable);


    // 6. Tìm kiếm theo tên + phân trang
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourDTO(
         t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
         t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
         t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
         c.id, c.name, c.icon
     )
     FROM Tour t
     LEFT JOIN t.destination d
     LEFT JOIN t.category c
     LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
     LEFT JOIN t.reviews r
     WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
       AND t.status != com.travel.travelbooking.entity.TourStatus.DELETED
     GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
              t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
              t.maxParticipants, c.id, c.name, c.icon
     ORDER BY t.id ASC
     """)
    Page<TourDTO> findByNameContainingIgnoreCaseWithCountsAndPageable(
            @Param("name") String name,
            Pageable pageable);

    // 7. Thống kê (không ảnh hưởng category)
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourStatsDTO(
         COUNT(DISTINCT t.id),
         COUNT(DISTINCT CASE WHEN t.status = 'ACTIVE' THEN t.id END),
         COUNT(DISTINCT CASE WHEN t.status = 'INACTIVE' THEN t.id END),
         COALESCE(SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0)
     )
     FROM Tour t
     LEFT JOIN t.bookings b
     WHERE t.status != com.travel.travelbooking.entity.TourStatus.DELETED
     """)
    TourStatsDTO getTourStats();

    // 8 Lấy tour theo category
    @Query("""
     SELECT new com.travel.travelbooking.dto.TourDTO(
         t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
         t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
         t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
         c.id, c.name, c.icon
     )
     FROM Tour t
     LEFT JOIN t.destination d
     LEFT JOIN t.category c
     LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
     LEFT JOIN t.reviews r
     WHERE t.category.id = :categoryId AND t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
     GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
              t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
              t.maxParticipants, c.id, c.name, c.icon
     ORDER BY t.createdAt DESC
     """)
    List<TourDTO> findByCategoryIdWithCounts(@Param("categoryId") Long categoryId);

    @Query("""
    SELECT new com.travel.travelbooking.dto.TourDTO(
        t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
        t.description, t.averageRating, COALESCE(t.totalParticipants, 0), t.status,
        t.createdAt, COUNT(b), COUNT(r), t.maxParticipants,
        c.id, c.name, c.icon
    )
    FROM Tour t
    LEFT JOIN t.destination d
    LEFT JOIN t.category c
    LEFT JOIN t.bookings b ON b.status = 'CONFIRMED'
    LEFT JOIN t.reviews r
    WHERE t.category.id = :categoryId
      AND t.status = com.travel.travelbooking.entity.TourStatus.ACTIVE
    GROUP BY t.id, t.name, d.id, d.name, t.duration, t.price, t.imageUrl,
             t.description, t.averageRating, t.totalParticipants, t.status, t.createdAt,
             t.maxParticipants, c.id, c.name, c.icon
    ORDER BY t.createdAt DESC
    """)
    Page<TourDTO> findByCategoryIdWithCountsPaged(@Param("categoryId") Long categoryId, Pageable pageable);
}