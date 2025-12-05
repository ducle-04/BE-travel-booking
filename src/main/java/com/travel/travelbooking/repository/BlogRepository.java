package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.Blog;
import com.travel.travelbooking.entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    // Lấy blog đã duyệt (phân trang)
    Page<Blog> findByStatusOrderByCreatedAtDesc(BlogStatus status, Pageable pageable);

    // Lấy chi tiết blog đã duyệt
    Optional<Blog> findByIdAndStatus(Long id, BlogStatus status);

    // API này vẫn giữ nhưng không dùng nữa
    List<Blog> findTop5ByIdNotAndStatusOrderByCreatedAtDesc(Long blogId, BlogStatus status);

    // Admin get all blogs → fetch join tránh lazy
    @Query("""
        SELECT DISTINCT b FROM Blog b
        LEFT JOIN FETCH b.user
        LEFT JOIN FETCH b.images
        ORDER BY b.createdAt DESC
    """)
    List<Blog> findAllWithUser();

    // ⭐ FIX LAZY LOADING CHO RELATED BLOGS
    @Query("""
        SELECT DISTINCT b FROM Blog b
        LEFT JOIN FETCH b.user
        LEFT JOIN FETCH b.images
        WHERE b.status = 'APPROVED'
        AND b.id <> :blogId
        ORDER BY b.createdAt DESC
    """)
    List<Blog> findRelatedBlogs(Long blogId);
}
