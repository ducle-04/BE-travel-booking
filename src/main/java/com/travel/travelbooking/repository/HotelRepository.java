package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h FROM Hotel h WHERE " +
            "(:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:address IS NULL OR LOWER(h.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
            "(:status IS NULL OR h.status = :status) AND " +
            "(:star IS NULL OR h.starRating = :star)")
    Page<Hotel> searchHotels(
            @Param("name") String name,
            @Param("address") String address,
            @Param("status") Hotel.HotelStatus status,
            @Param("star") Integer star,
            Pageable pageable);

    long countByStatus(Hotel.HotelStatus status);
    long countByStarRating(Integer starRating);
}