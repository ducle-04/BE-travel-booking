package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.BlogCommentDTO;
import com.travel.travelbooking.Dto.BlogDTO;
import com.travel.travelbooking.Dto.BlogSummaryDTO;
import com.travel.travelbooking.Dto.CreateCommentDTO;
import com.travel.travelbooking.Entity.*;
import com.travel.travelbooking.Exception.ResourceNotFoundException;
import com.travel.travelbooking.Repository.BlogCommentRepository;
import com.travel.travelbooking.Repository.BlogRepository;
import com.travel.travelbooking.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final UserRepository userRepository;

    // Luồng chính 2, 3, 4: Lấy danh sách blog đã xuất bản (phân trang)
    @Transactional(readOnly = true)
    public Page<BlogSummaryDTO> getPublishedBlogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Blog> blogPage = blogRepository.findByStatusOrderByCreatedAtDesc(BlogStatus.APPROVED, pageable);
        return blogPage.map(this::toBlogSummaryDTO);
    }

    // Luồng chính 5, 6, 7: Lấy chi tiết blog và tăng lượt xem
    @Transactional
    public BlogDTO getPublishedBlogById(Long id) {
        Blog blog = blogRepository.findByIdAndStatus(id, BlogStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không khả dụng")); // Luồng rẽ nhánh R1

        // Luồng chính 7: Ghi nhận lượt xem
        blog.setViews(blog.getViews() + 1);
        blogRepository.save(blog);

        return toBlogDTO(blog);
    }

    // Luồng chính 8: Lấy bài viết liên quan
    @Transactional(readOnly = true)
    public List<BlogSummaryDTO> getRelatedBlogs(Long blogId) {
        // Lấy 5 bài mới nhất, trừ bài hiện tại
        List<Blog> blogs = blogRepository.findTop5ByIdNotAndStatusOrderByCreatedAtDesc(blogId, BlogStatus.APPROVED);
        return blogs.stream()
                .map(this::toBlogSummaryDTO)
                .collect(Collectors.toList());
    }

    // Lấy danh sách bình luận (đã duyệt)
    @Transactional(readOnly = true)
    public List<BlogCommentDTO> getComments(Long blogId) {
        List<BlogComment> comments = blogCommentRepository
                .findByBlogIdAndStatusOrderByCreatedAtDesc(blogId, CommentStatus.APPROVED);
        
        return comments.stream()
                .map(this::toBlogCommentDTO)
                .collect(Collectors.toList());
    }

    // Luồng chính 9, 10: Gửi bình luận
    @Transactional
    public BlogCommentDTO createComment(Long blogId, CreateCommentDTO dto, UserDetails userDetails) {
        // R3: Đã được xử lý bởi SecurityConfig (chỉ user đăng nhập mới vào được)
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username);
        
        Blog blog = blogRepository.findByIdAndStatus(blogId, BlogStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không khả dụng"));

        BlogComment comment = new BlogComment();
        comment.setContent(dto.getContent());
        comment.setUser(user);
        comment.setBlog(blog);
        // Trạng thái PENDING được tự set bởi @PrePersist (Hậu điều kiện)

        BlogComment savedComment = blogCommentRepository.save(comment);
        return toBlogCommentDTO(savedComment);
    }

    // === Helper Methods ===

    private BlogDTO toBlogDTO(Blog blog) {
        BlogDTO dto = new BlogDTO();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setContent(blog.getContent());
        dto.setThumbnail(blog.getThumbnail());
        dto.setAuthorName(blog.getUser().getFullname());
        dto.setAuthorId(blog.getUser().getId());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());
        dto.setViews(blog.getViews());
        if (blog.getImages() != null) {
            dto.setImages(blog.getImages().stream()
                    .map(BlogImage::getImageUrl)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private BlogSummaryDTO toBlogSummaryDTO(Blog blog) {
        BlogSummaryDTO dto = new BlogSummaryDTO();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setThumbnail(blog.getThumbnail());
        dto.setAuthorName(blog.getUser().getFullname());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setViews(blog.getViews());

        // Lấy mô tả ngắn (ví dụ: 150 ký tự đầu của content)
        if (blog.getContent() != null) {
            String strippedContent = blog.getContent().replaceAll("<[^>]*>", ""); // Xóa HTML
            dto.setShortDescription(strippedContent.length() > 150 ? 
                strippedContent.substring(0, 150) + "..." : strippedContent);
        }
        
        // Đếm số bình luận đã duyệt
        if (blog.getComments() != null) {
            dto.setCommentCount((int) blog.getComments().stream()
                    .filter(c -> c.getStatus() == CommentStatus.APPROVED)
                    .count());
        } else {
            dto.setCommentCount(0);
        }
        return dto;
    }

    private BlogCommentDTO toBlogCommentDTO(BlogComment comment) {
        BlogCommentDTO dto = new BlogCommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserAvatarUrl(comment.getUser().getAvatarUrl());
        return dto;
    }
}