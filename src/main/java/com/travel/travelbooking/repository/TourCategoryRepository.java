package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.CategoryStatus;
import com.travel.travelbooking.entity.TourCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourCategoryRepository extends JpaRepository<TourCategory, Long> {

    // Phân trang cho admin
    Page<TourCategory> findAll(Pageable pageable);

    // Lấy danh sách loại tour đang active, sắp xếp theo thứ tự hiển thị
    List<TourCategory> findByStatusOrderByDisplayOrderAsc(CategoryStatus status);

    // Tìm theo tên (không phân biệt hoa thường)
    Optional<TourCategory> findByNameIgnoreCase(String name);

    // Kiểm tra tên đã tồn tại chưa
    boolean existsByNameIgnoreCase(String name);

    // Kiểm tra tên đã tồn tại nhưng loại trừ chính nó (khi update)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Bonus: lấy theo status
    List<TourCategory> findByStatus(CategoryStatus status);
}