package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.BlogCommentDTO;
import com.travel.travelbooking.dto.BlogDTO;
import com.travel.travelbooking.dto.BlogSummaryDTO;
import com.travel.travelbooking.dto.CreateCommentDTO;
import com.travel.travelbooking.dto.BlogCreateDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.BlogCommentRepository;
import com.travel.travelbooking.repository.BlogRepository;
import com.travel.travelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    //User đăng blog
    @Override // Thêm @Override
    @Transactional
    public BlogDTO createBlog(BlogCreateDTO dto, MultipartFile thumbnailFile,
                            List<MultipartFile> imageFiles, UserDetails userDetails) throws IOException {
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }

        // 1. Tạo Blog entity
        Blog blog = new Blog();
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setUser(user);
        // blog.setStatus(BlogStatus.PENDING); // Đã được set mặc định trong Entity Blog.java

        // 2. Upload và set thumbnail
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            throw new IllegalArgumentException("Ảnh thumbnail là bắt buộc");
        }
        String thumbnailUrl = cloudinaryService.uploadImage(thumbnailFile);
        blog.setThumbnail(thumbnailUrl);

        // 3. Upload và set ảnh chi tiết (nếu có)
        List<BlogImage> blogImages = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imgUrl = cloudinaryService.uploadImage(file);
                    BlogImage blogImage = new BlogImage();
                    blogImage.setImageUrl(imgUrl);
                    blogImage.setBlog(blog); //Liên kết ảnh với blog
                    blogImages.add(blogImage);
                }
            }
        }
        blog.setImages(blogImages);

        // 4. Lưu blog (và BlogImage nhờ CascadeType.ALL)
        Blog savedBlog = blogRepository.save(blog);
        return toBlogDTO(savedBlog);
    }

    // Admin/Staff duyệt blog
    @Override // Thêm @Override
    @Transactional
    public BlogDTO approveBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài blog với ID: " + blogId));

        if (blog.getStatus() != BlogStatus.PENDING) {
            throw new IllegalArgumentException("Blog không ở trạng thái chờ duyệt");
        }
        
        blog.setStatus(BlogStatus.APPROVED);
        return toBlogDTO(blogRepository.save(blog));
    }

    @Override // Thêm @Override
    @Transactional
    public BlogDTO rejectBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài blog với ID: " + blogId));

        if (blog.getStatus() != BlogStatus.PENDING) {
            throw new IllegalArgumentException("Blog không ở trạng thái chờ duyệt");
        }
        
        blog.setStatus(BlogStatus.REJECTED);
        return toBlogDTO(blogRepository.save(blog));
    }

    // Luồng chính 2, 3, 4: Lấy danh sách blog đã xuất bản (phân trang)
    @Override // Thêm @Override
    @Transactional(readOnly = true)
    public Page<BlogSummaryDTO> getPublishedBlogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Blog> blogPage = blogRepository.findByStatusOrderByCreatedAtDesc(BlogStatus.APPROVED, pageable);
        return blogPage.map(this::toBlogSummaryDTO);
    }

    // Luồng chính 5, 6, 7: Lấy chi tiết blog và tăng lượt xem
    @Override // Thêm @Override
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
    @Override // Thêm @Override
    @Transactional(readOnly = true)
    public List<BlogSummaryDTO> getRelatedBlogs(Long blogId) {
        // Lấy 5 bài mới nhất, trừ bài hiện tại
        List<Blog> blogs = blogRepository.findTop5ByIdNotAndStatusOrderByCreatedAtDesc(blogId, BlogStatus.APPROVED);
        return blogs.stream()
                .map(this::toBlogSummaryDTO)
                .collect(Collectors.toList());
    }

    // Lấy danh sách bình luận (đã duyệt)
    @Override // Thêm @Override
    @Transactional(readOnly = true)
    public List<BlogCommentDTO> getComments(Long blogId) {
        List<BlogComment> comments = blogCommentRepository
                .findByBlogIdAndStatusOrderByCreatedAtDesc(blogId, CommentStatus.APPROVED);
        
        return comments.stream()
                .map(this::toBlogCommentDTO)
                .collect(Collectors.toList());
    }

    // Luồng chính 9, 10: Gửi bình luận
    @Override // Thêm @Override
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