package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.cloudinary.CloudinaryService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto.CategoryResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.service.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    // ─── POST: Tạo category mới (ADMIN only) ──────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo danh mục mới thành công",
                categoryService.createCategory(request)));
    }

    // ─── GET: Lấy chi tiết category theo id (public) ─────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết danh mục thành công",
                categoryService.getCategoryById(id)));
    }

    // ─── GET: Danh sách có phân trang/lọc (public) ───────────────────────────
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

    // ─── GET /simple: Danh sách rút gọn, chỉ active (public, cho dropdown) ───
    @GetMapping("/simple")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getSimpleCategories() {
        List<CategoryResponseDTO> categories = categoryService.getSimpleCategories();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách danh mục thành công", categories));
    }

    // ─── PATCH: Cập nhật category (ADMIN only) ────────────────────────────────
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật danh mục thành công",
                categoryService.updateCategory(id, request)));
    }

    // ─── DELETE: Xóa category (ADMIN only) ───────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công"));
    }

    // ─── POST /{id}/upload-image: Upload ảnh đại diện (ADMIN only) ───────────
    @PostMapping("/{id}/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadCategoryImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        // Kiểm tra category tồn tại
        categoryService.getCategoryById(id);

        // Upload lên Cloudinary
        Map<String, String> result = cloudinaryService.upload(image, "categories");
        String imageUrl = result.get("url");

        // Cập nhật imageUrl vào category
        CategoryRequestDTO updateRequest = CategoryRequestDTO.builder()
                .imageUrl(imageUrl)
                .build();
        categoryService.updateCategory(id, updateRequest);

        return ResponseEntity.ok(ApiResponse.success(
                "Upload ảnh danh mục thành công", Map.of("imageUrl", imageUrl)));
    }
}
