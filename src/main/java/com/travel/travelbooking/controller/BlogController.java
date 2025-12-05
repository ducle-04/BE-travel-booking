package com.travel.travelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.travelbooking.dto.BlogCommentDTO;
import com.travel.travelbooking.dto.BlogDTO;
import com.travel.travelbooking.dto.BlogSummaryDTO;
import com.travel.travelbooking.dto.CreateCommentDTO;
import com.travel.travelbooking.dto.BlogCreateDTO;
import com.travel.travelbooking.payload.ApiResponse;
import com.travel.travelbooking.service.BlogService;
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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogSummaryDTO>>> getBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BlogSummaryDTO> blogs = blogService.getPublishedBlogs(page, size);
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách blog thành công", blogs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogDTO>> getBlogById(@PathVariable Long id) {
        BlogDTO blog = blogService.getPublishedBlogById(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bài viết thành công", blog));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<BlogSummaryDTO>>> getRelatedBlogs(@PathVariable Long id) {
        List<BlogSummaryDTO> blogs = blogService.getRelatedBlogs(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bài viết liên quan thành công", blogs));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<BlogCommentDTO>>> getComments(@PathVariable Long id) {
        List<BlogCommentDTO> comments = blogService.getComments(id);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bình luận thành công", comments));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BlogDTO>> createBlog(
            @RequestPart("blog") @Valid String blogJson,
            @RequestPart("thumbnail") MultipartFile thumbnailFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        BlogCreateDTO dto = mapper.readValue(blogJson, BlogCreateDTO.class);

        BlogDTO newBlog = blogService.createBlog(dto, thumbnailFile, images, userDetails);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Blog đã được gửi, đang chờ duyệt", newBlog));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BlogCommentDTO>> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        BlogCommentDTO newComment = blogService.createComment(id, dto, userDetails);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>("Gửi bình luận thành công, đang chờ duyệt", newComment));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<BlogDTO>>> getAllBlogsForAdmin() {
        List<BlogDTO> blogs = blogService.getAllBlogsForAdmin();
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách blog thành công", blogs));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BlogDTO>> approveBlog(@PathVariable Long id) {
        BlogDTO approvedBlog = blogService.approveBlog(id);
        return ResponseEntity.ok(new ApiResponse<>("Đã duyệt bài blog", approvedBlog));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<BlogDTO>> rejectBlog(@PathVariable Long id) {
        BlogDTO rejectedBlog = blogService.rejectBlog(id);
        return ResponseEntity.ok(new ApiResponse<>("Đã từ chối bài blog", rejectedBlog));
    }

    // ⭐⭐ API DELETE — THÊM MỚI ⭐⭐
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<String>> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok(new ApiResponse<>("Đã xóa blog thành công", "OK"));
    }
}
