package vn.edu.hcmuaf.fit.artisanMarket.modules.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.service.ProductService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;

    // ── PUBLIC ENDPOINTS ─────────────────────────────────────────────────────

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết sản phẩm thành công",
                productService.getProductById(id)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getAllProducts(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {
        Page<ProductResponseDTO> products = productService.getAllProducts(page, size, search, categoryId, isActive,
                sortBy);
        return ResponseEntity.ok(ApiResponse.success(products,"Lấy danh sách sản phẩm thành công"));
    }

    // ── ARTISAN ENDPOINTS ────────────────────────────────────────────────────

    /**
     * Lấy danh sách sản phẩm của Artisan đang đăng nhập
     */
    @GetMapping("/artisan/products")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getMyProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long artisanId = resolveArtisanId();
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getMyProducts(artisanId, pageable),"Lấy danh sách sản phẩm của bạn thành công"));
    }

    /**
     * Thêm sản phẩm mới — nhận multipart/form-data
     */
    @PostMapping(value = "/artisan/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @ModelAttribute @Valid ProductRequestDTO request) {
        Long artisanId = resolveArtisanId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Thêm sản phẩm mới thành công",
                        productService.createProduct(artisanId, request)));
    }

    /**
     * Cập nhật sản phẩm
     */
    @PutMapping(value = "/artisan/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @ModelAttribute @Valid ProductRequestDTO request) {
        Long artisanId = resolveArtisanId();
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật sản phẩm thành công",
                productService.updateProduct(artisanId, id, request)));
    }

    /**
     * Ẩn / hiện sản phẩm
     */
    @PatchMapping("/artisan/products/{id}/status")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        Long artisanId = resolveArtisanId();
        productService.toggleProductStatus(artisanId, id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái sản phẩm thành công"));
    }

    /**
     * Xoá sản phẩm (soft delete)
     */
    @DeleteMapping("/artisan/products/{id}")
    @PreAuthorize("hasRole('ARTISAN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        Long artisanId = resolveArtisanId();
        productService.deleteProduct(artisanId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công"));
    }

    // ── ADMIN ENDPOINTS ──────────────────────────────────────────────────────

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> adminGetAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        return ResponseEntity.ok(ApiResponse.success(
                productService.getAllProductsForAdmin(pageable, keyword, status),
                "Lấy danh sách tất cả sản phẩm thành công"));
    }

    @PostMapping(value = "/admin/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> adminCreateProduct(
            @ModelAttribute @Valid ProductRequestDTO request,
            @RequestParam Long artisanId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Thêm sản phẩm thành công (Admin)",
                        productService.createProduct(artisanId, request)));
    }

    @PutMapping(value = "/admin/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> adminUpdateProduct(
            @PathVariable Long id,
            @ModelAttribute @Valid ProductRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật sản phẩm thành công (Admin)",
                productService.updateProduct(id, request)));
    }

    @PatchMapping("/admin/products/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminToggleStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        productService.adminToggleStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái sản phẩm thành công"));
    }

    // ── PRIVATE HELPER ───────────────────────────────────────────────────────

    private Long resolveArtisanId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));
        Artisan artisan = artisanRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Hồ sơ nghệ nhân không tồn tại"));
        return artisan.getId();
    }
}
