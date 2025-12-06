package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.ReviewCreateRequest;
import com.travel.travelbooking.dto.ReviewDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReviewService {
    ReviewDTO createReview(Long tourId, ReviewCreateRequest request, Long userId);
    List<ReviewDTO> getReviewsByTour(Long tourId);
    Page<ReviewDTO> getReviewsByTourPaged(Long tourId, int page, int size);
    boolean canUserReviewTour(Long tourId, Long userId);
}