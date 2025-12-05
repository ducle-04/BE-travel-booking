package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.*;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.BlogCommentRepository;
import com.travel.travelbooking.repository.BlogRepository;
import com.travel.travelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // ===================== CREATE BLOG =====================
    @Override
    @Transactional
    public BlogDTO createBlog(BlogCreateDTO dto, MultipartFile thumbnailFile,
                              List<MultipartFile> imageFiles, UserDetails userDetails) throws IOException {

        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) throw new ResourceNotFoundException("Không tìm thấy người dùng");

        Blog blog = new Blog();
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setUser(user);

        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            throw new IllegalArgumentException("Ảnh thumbnail là bắt buộc");
        }

        blog.setThumbnail(cloudinaryService.uploadImage(thumbnailFile));

        List<BlogImage> images = new ArrayList<>();
        if (imageFiles != null) {
            for (MultipartFile f : imageFiles) {
                if (f != null && !f.isEmpty()) {
                    BlogImage img = new BlogImage();
                    img.setImageUrl(cloudinaryService.uploadImage(f));
                    img.setBlog(blog);
                    images.add(img);
                }
            }
        }

        blog.setImages(images);
        return toBlogDTO(blogRepository.save(blog));
    }

    // ===================== ADMIN GET ALL BLOGS =====================
    @Override
    public List<BlogDTO> getAllBlogsForAdmin() {
        return blogRepository.findAllWithUser()
                .stream()
                .map(this::toBlogDTO)
                .collect(Collectors.toList());
    }

    // ===================== APPROVE BLOG =====================
    @Override
    @Transactional
    public BlogDTO approveBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài blog"));

        if (blog.getStatus() != BlogStatus.PENDING)
            throw new IllegalArgumentException("Blog không ở trạng thái chờ duyệt");

        blog.setStatus(BlogStatus.APPROVED);
        return toBlogDTO(blogRepository.save(blog));
    }

    // ===================== REJECT BLOG =====================
    @Override
    @Transactional
    public BlogDTO rejectBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài blog"));

        if (blog.getStatus() != BlogStatus.PENDING)
            throw new IllegalArgumentException("Blog không ở trạng thái chờ duyệt");

        blog.setStatus(BlogStatus.REJECTED);
        return toBlogDTO(blogRepository.save(blog));
    }

    // ===================== DELETE BLOG =====================
    @Override
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài blog"));

        blogRepository.delete(blog);
    }

    // ===================== GET PUBLISHED BLOGS =====================
    @Override
    @Transactional(readOnly = true)
    public Page<BlogSummaryDTO> getPublishedBlogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return blogRepository.findByStatusOrderByCreatedAtDesc(BlogStatus.APPROVED, pageable)
                .map(this::toBlogSummaryDTO);
    }

    // ===================== GET BLOG BY ID =====================
    @Override
    @Transactional
    public BlogDTO getPublishedBlogById(Long id) {
        Blog blog = blogRepository.findByIdAndStatus(id, BlogStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không khả dụng"));

        blog.setViews(blog.getViews() + 1);
        blogRepository.save(blog);

        return toBlogDTO(blog);
    }

    // ===================== RELATED BLOGS (FIX LAZY) =====================
    @Override
    public List<BlogSummaryDTO> getRelatedBlogs(Long blogId) {
        return blogRepository.findRelatedBlogs(blogId)
                .stream()
                .limit(5)
                .map(this::toBlogSummaryDTO)
                .collect(Collectors.toList());
    }

    // ===================== GET COMMENTS =====================
    @Override
    public List<BlogCommentDTO> getComments(Long blogId) {
        return blogCommentRepository
                .findByBlogIdAndStatusOrderByCreatedAtDesc(blogId, CommentStatus.APPROVED)
                .stream()
                .map(this::toBlogCommentDTO)
                .collect(Collectors.toList());
    }

    // ===================== CREATE COMMENT =====================
    @Override
    @Transactional
    public BlogCommentDTO createComment(Long blogId, CreateCommentDTO dto, UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());

        Blog blog = blogRepository.findByIdAndStatus(blogId, BlogStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không khả dụng"));

        BlogComment c = new BlogComment();
        c.setContent(dto.getContent());
        c.setUser(user);
        c.setBlog(blog);

        return toBlogCommentDTO(blogCommentRepository.save(c));
    }

    // ===================== DTO MAPPERS =====================

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
        dto.setStatus(blog.getStatus());

        dto.setImages(
                blog.getImages() == null ? new ArrayList<>() :
                        blog.getImages().stream().map(BlogImage::getImageUrl).collect(Collectors.toList())
        );

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

        if (blog.getContent() != null) {
            String stripped = blog.getContent().replaceAll("<[^>]*>", "");
            dto.setShortDescription(stripped.length() > 150 ? stripped.substring(0, 150) + "..." : stripped);
        }

        dto.setCommentCount(
                blogCommentRepository.countByBlogIdAndStatus(blog.getId(), CommentStatus.APPROVED)
        );

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
