package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.TourCategoryDTO;
import java.util.List;

public interface TourCategoryService {
    List<TourCategoryDTO> getActiveCategories();
    List<TourCategoryDTO> getAllCategories();
    TourCategoryDTO create(TourCategoryDTO dto);
    TourCategoryDTO update(Long id, TourCategoryDTO dto);
    void delete(Long id);
}