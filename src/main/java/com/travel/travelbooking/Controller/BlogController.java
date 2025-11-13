package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.BlogCommentDTO;
import com.travel.travelbooking.Dto.BlogDTO;
import com.travel.travelbooking.Dto.BlogSummaryDTO;
import com.travel.travelbooking.Dto.CreateCommentDTO;
import com.travel.travelbooking.Payload.ApiResponse;
import com.travel.travelbooking.Service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    // Luồng chính 1-4: Lấy danh sách bài viết (Public)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogSummaryDTO>>> getBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BlogSummaryDTO> blogs = blogService.getPublishedBlogs(page, size);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách blog thành công", blogs));
    }

    // Luồng chính 5-7: Lấy chi tiết bài viết (Public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogDTO>> getBlogById(@PathVariable Long id) {
        BlogDTO blog = blogService.getPublishedBlogById(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bài viết thành công", blog));
    }

    // Luồng chính 8: Lấy bài viết liên quan (Public)
    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<BlogSummaryDTO>>> getRelatedBlogs(@PathVariable Long id) {
        List<BlogSummaryDTO> blogs = blogService.getRelatedBlogs(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bài viết liên quan thành công", blogs));
    }

    // Lấy bình luận của bài viết (Public)
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<BlogCommentDTO>>> getComments(@PathVariable Long id) {
        List<BlogCommentDTO> comments = blogService.getComments(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bình luận thành công", comments));
    }

    // Luồng chính 9-10 & R3, R4: Gửi bình luận (Authenticated)
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()") // R3: Yêu cầu đăng nhập
    public ResponseEntity<ApiResponse<BlogCommentDTO>> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentDTO dto, // R4: Validate nội dung
            @AuthenticationPrincipal UserDetails userDetails) {
        
        BlogCommentDTO newComment = blogService.createComment(id, dto, userDetails);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Gửi bình luận thành công, đang chờ duyệt", newComment));
    }
}