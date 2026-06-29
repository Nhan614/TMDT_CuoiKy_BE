package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(@RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo danh mục mới thành công",
                categoryService.createCategory(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết danh mục thành công",
                categoryService.getCategoryById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponseDTO>>> getAllCategories(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "id_desc") String sortBy) {
        Page<CategoryResponseDTO> categories = categoryService.getAllCategories(page, size, search, parentId, isActive,
                sortBy);
        return ResponseEntity.ok(ApiResponse.success(
                categories, "Lấy danh sách danh mục thành công"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật danh mục thành công",
                categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công"));
    }
}
