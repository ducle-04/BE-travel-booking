package com.travel.travelbooking.Entity.Repository;

import com.travel.travelbooking.Entity.BlogComment;
import com.travel.travelbooking.Entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    // Lấy các bình luận đã được duyệt của một bài blog
    List<BlogComment> findByBlogIdAndStatusOrderByCreatedAtDesc(Long blogId, CommentStatus status);
}