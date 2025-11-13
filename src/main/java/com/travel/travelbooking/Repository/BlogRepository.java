package com.travel.travelbooking.Entity.Repository;

import com.travel.travelbooking.Entity.Blog;
import com.travel.travelbooking.Entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    //Lấy bài viết đã xuất bản (APPROVED)
    Page<Blog> findByStatusOrderByCreatedAtDesc(BlogStatus status, Pageable pageable);

    //Lấy chi tiết bài viết
    Optional<Blog> findByIdAndStatus(Long id, BlogStatus status);

    //Lấy bài viết liên quan
    List<Blog> findTop5ByIdNotAndStatusOrderByCreatedAtDesc(Long blogId, BlogStatus status);
}