package com.travel.travelbooking.controller;

import com.travel.travelbooking.dto.ReviewCreateRequest;
import com.travel.travelbooking.dto.ReviewDTO;
import com.travel.travelbooking.entity.User;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.ReviewService;
import com.travel.travelbooking.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserServiceImpl userService;

    private User validateUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập");
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null || user.getStatus() == null || "DELETED".equals(user.getStatus().name())) {
            throw new IllegalArgumentException("Tài khoản không hợp lệ hoặc đã bị xóa");
        }

        return user;
    }

    // 1. Tạo đánh giá
    @PostMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tourId,
            @Valid @RequestBody ReviewCreateRequest request) {

        User user = validateUser(userDetails);
        ReviewDTO dto = reviewService.createReview(tourId, request, user.getId());
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Đánh giá thành công", dto));
    }

    // 2. Lấy đánh giá của tour
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getReviews(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewDTO> reviews = reviewService.getReviewsByTourPaged(tourId, page, size);
        return ResponseEntity.ok(new ApiResponse<>("Lấy đánh giá thành công", reviews));
    }

    // 3. Kiểm tra có được đánh giá không (dùng ở FE)
    @GetMapping("/tour/{tourId}/can-review")
    public ResponseEntity<ApiResponse<Boolean>> canReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tourId) {

        if (userDetails == null) {
            return ResponseEntity.ok(new ApiResponse<>("Chưa đăng nhập", false));
        }

        User user = userService.findByUsername(userDetails.getUsername());
        boolean can = reviewService.canUserReviewTour(tourId, user.getId());
        return ResponseEntity.ok(new ApiResponse<>("Kiểm tra quyền đánh giá", can));
    }
}
