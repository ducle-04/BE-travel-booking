package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.BlogComment;
import com.travel.travelbooking.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    int countByBlogIdAndStatus(Long blogId, CommentStatus status);
    // Lấy các bình luận đã được duyệt của một bài blog
    List<BlogComment> findByBlogIdAndStatusOrderByCreatedAtDesc(Long blogId, CommentStatus status);
}