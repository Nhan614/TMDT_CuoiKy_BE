package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryResponseDTO;

public interface CategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO request);
    CategoryResponseDTO getCategoryById(Long id);
    Page<CategoryResponseDTO> getAllCategories(int page, int size, String search, Long parentId, Boolean isActive, String sortBy);
    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request);
    void deleteCategory(Long id);
}
