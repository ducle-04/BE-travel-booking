package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.ReviewCreateRequest;
import com.travel.travelbooking.dto.ReviewDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.ReviewRepository;
import com.travel.travelbooking.repository.TourRepository;
import com.travel.travelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewDTO createReview(Long tourId, ReviewCreateRequest request, Long userId) {
        // 1. Kiểm tra tour tồn tại + ACTIVE
        Tour tour = tourRepository.findById(tourId)
                .filter(t -> t.getStatus() == TourStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại hoặc đã bị xóa"));

        // 2. Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // 3. Kiểm tra đã từng đánh giá chưa
        if (reviewRepository.existsByUserIdAndTourId(userId, tourId)) {
            throw new IllegalArgumentException("Bạn đã đánh giá tour này rồi");
        }

        // 4. Kiểm tra có booking COMPLETED không
        boolean hasCompletedBooking = bookingRepository.existsByUserIdAndTourIdAndStatus(
                userId, tourId, BookingStatus.COMPLETED
        );

        if (!hasCompletedBooking) {
            throw new IllegalArgumentException("Bạn chỉ có thể đánh giá tour đã hoàn thành");
        }

        // 5. Tạo review
        Review review = new Review();
        review.setUser(user);
        review.setTour(tour);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);

        // 6. Cập nhật averageRating cho tour
        Double avg = reviewRepository.calculateAverageRating(tourId);
        tour.setAverageRating(avg);
        tourRepository.save(tour);

        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByTour(Long tourId) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));

        return reviewRepository.findByTourIdOrderByCreatedAtDesc(tourId)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByTourPaged(Long tourId, int page, int size) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour không tồn tại"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findByTourIdOrderByCreatedAtDesc(tourId, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewTour(Long tourId, Long userId) {
        if (!tourRepository.existsById(tourId)) return false;

        if (reviewRepository.existsByUserIdAndTourId(userId, tourId)) return false;

        return bookingRepository.existsByUserIdAndTourIdAndStatus(
                userId, tourId, BookingStatus.COMPLETED
        );
    }

    private ReviewDTO toDTO(Review r) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUser().getId());
        dto.setUserFullname(r.getUser().getFullname());
        dto.setUserAvatarUrl(r.getUser().getAvatarUrl());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setTourId(r.getTour().getId());
        dto.setTourName(r.getTour().getName());
        return dto;
    }
}
