package com.travel.travelbooking.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.Dto.BlogCommentDTO;
import com.travel.travelbooking.Dto.BlogDTO;
import com.travel.travelbooking.Dto.BlogSummaryDTO;
import com.travel.travelbooking.Dto.CreateCommentDTO;
import com.travel.travelbooking.Dto.BlogCreateDTO;
import com.travel.travelbooking.Payload.ApiResponse;
import com.travel.travelbooking.Service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // Luồng chính 9-10 & R2: Tạo bài viết mới (Authenticated)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()") // Bất kỳ ai đăng nhập đều có thể gửi bài
    public ResponseEntity<ApiResponse<BlogDTO>> createBlog(
            @RequestPart("blog") @Valid String blogJson,
            @RequestPart("thumbnail") MultipartFile thumbnailFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        // Dùng ObjectMapper để chuyển JSON string thành DTO
        ObjectMapper mapper = new ObjectMapper();
        BlogCreateDTO dto = mapper.readValue(blogJson, BlogCreateDTO.class);

        BlogDTO newBlog = blogService.createBlog(dto, thumbnailFile, images, userDetails);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Blog đã được gửi, đang chờ duyệt", newBlog));
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

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')") // Giống phân quyền của Tour
    public ResponseEntity<ApiResponse<BlogDTO>> approveBlog(@PathVariable Long id) {
        BlogDTO approvedBlog = blogService.approveBlog(id);
        return ResponseEntity.ok(new ApiResponse<>("Đã duyệt bài blog", approvedBlog));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<BlogDTO>> rejectBlog(@PathVariable Long id) {
        BlogDTO rejectedBlog = blogService.rejectBlog(id);
        return ResponseEntity.ok(new ApiResponse<>("Đã từ chối bài blog", rejectedBlog));
    }
}