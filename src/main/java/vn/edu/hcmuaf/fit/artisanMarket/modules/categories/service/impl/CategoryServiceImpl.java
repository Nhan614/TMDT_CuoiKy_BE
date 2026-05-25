package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.Category;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.domain.repository.CategoryRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service.CategoryService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private CategoryResponseDTO toDTO(Category category) {
        return CategoryResponseDTO.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Đường dẫn thân thiện (slug) của danh mục đã tồn tại");
        }

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        Category parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(parent)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return toDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        return toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> getAllCategories(int page, int size, String search, Long parentId, Boolean isActive, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "name_asc":
                    sort = Sort.by(Sort.Direction.ASC, "name");
                    break;
                case "name_desc":
                    sort = Sort.by(Sort.Direction.DESC, "name");
                    break;
                case "id_desc":
                    sort = Sort.by(Sort.Direction.DESC, "id");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size, sort);
        Page<Category> categories = categoryRepository.findCategories(search, parentId, isActive, pageable);
        return categories.map(this::toDTO);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new RuntimeException("Đường dẫn thân thiện (slug) của danh mục đã tồn tại");
            }
            category.setSlug(request.getSlug());
        }

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new RuntimeException("Tên danh mục đã tồn tại");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        if (request.getParentId() != null) {
            if (request.getParentId() == -1L) {
                category.setParent(null);
            } else {
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại"));
                category.setParent(parent);
            }
        }

        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }

        Category updatedCategory = categoryRepository.save(category);
        return toDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        categoryRepository.delete(category);
    }
}
