package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.TourCategoryDTO;
import com.travel.travelbooking.entity.CategoryStatus;
import com.travel.travelbooking.entity.TourCategory;
import com.travel.travelbooking.exception.ResourceNotFoundException;
import com.travel.travelbooking.repository.TourCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourCategoryServiceImpl implements TourCategoryService {

    private final TourCategoryRepository repository;

    @Override
    public List<TourCategoryDTO> getActiveCategories() {
        return repository.findByStatusOrderByDisplayOrderAsc(CategoryStatus.ACTIVE)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<TourCategoryDTO> getAllCategories() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public TourCategoryDTO create(TourCategoryDTO dto) {
        if (repository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new IllegalArgumentException("Tên loại tour đã tồn tại");
        }
        TourCategory entity = new TourCategory();
        copyDtoToEntity(dto, entity);
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public TourCategoryDTO update(Long id, TourCategoryDTO dto) {
        TourCategory entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại tour không tồn tại"));

        if (repository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), id)) {
            throw new IllegalArgumentException("Tên loại tour đã tồn tại");
        }

        copyDtoToEntity(dto, entity);
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TourCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại tour không tồn tại"));
        category.setStatus(CategoryStatus.INACTIVE);
        repository.save(category);
    }

    private void copyDtoToEntity(TourCategoryDTO dto, TourCategory entity) {
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setIcon(dto.getIcon());
        entity.setDisplayOrder(dto.getDisplayOrder());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : CategoryStatus.ACTIVE);
    }

    private TourCategoryDTO toDTO(TourCategory entity) {
        return new TourCategoryDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIcon(),
                entity.getDisplayOrder(),
                entity.getStatus()
        );
    }
}