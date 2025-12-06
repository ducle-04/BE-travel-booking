package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    Optional<Review> findByUserIdAndTourId(Long userId, Long tourId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.tour.id = :tourId")
    Double calculateAverageRating(@Param("tourId") Long tourId);

    List<Review> findByTourIdOrderByCreatedAtDesc(Long tourId);

    Page<Review> findByTourIdOrderByCreatedAtDesc(Long tourId, Pageable pageable);
}