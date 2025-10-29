package com.travel.travelbooking.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "blogs")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tiêu đề bài viết
    @Column(nullable = false, length = 200)
    private String title;

    // Nội dung bài viết
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    // Ảnh đại diện (URL ảnh hiển thị ngoài danh sách blog)
    private String thumbnail;

    // Tác giả bài blog (liên kết tới User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Danh sách ảnh chi tiết của blog (1 blog có nhiều ảnh)
    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BlogImage> images;

    // Ngày tạo
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Ngày cập nhật
    private LocalDateTime updatedAt;

    // Trạng thái duyệt bài
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogStatus status = BlogStatus.PENDING;

    // Lượt xem bài viết
    @Column(nullable = false)
    private int views = 0;

    // Tự động cập nhật thời gian khi update
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
