package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.TourDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TourDetailRepository extends JpaRepository<TourDetail, Long> {
    Optional<TourDetail> findByTourId(Long tourId);

}