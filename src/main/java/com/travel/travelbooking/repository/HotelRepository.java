package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByNameContainingIgnoreCase(String name);
    Optional<Hotel> findByNameIgnoreCase(String name);

    // THÊM DÒNG NÀY
    Page<Hotel> findByNameContainingIgnoreCase(String name, Pageable pageable);
}