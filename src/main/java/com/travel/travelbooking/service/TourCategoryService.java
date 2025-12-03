package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.TourCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TourCategoryService {
    List<TourCategoryDTO> getActiveCategories();
    List<TourCategoryDTO> getAllCategories(); // giữ lại cho các chỗ cũ
    Page<TourCategoryDTO> getAllCategoriesPaged(Pageable pageable); // ← THÊM MỚI
    TourCategoryDTO create(TourCategoryDTO dto);
    TourCategoryDTO update(Long id, TourCategoryDTO dto);
    void delete(Long id);
}