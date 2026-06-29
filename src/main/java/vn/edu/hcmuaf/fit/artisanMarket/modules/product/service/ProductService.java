package vn.edu.hcmuaf.fit.artisanMarket.modules.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);
    ProductResponseDTO getProductById(Long id);
    Page<ProductResponseDTO> getAllProducts(int page, int size, String search, Long categoryId, Boolean isActive, String sortBy);
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);
    void deleteProduct(Long id);

    // Artisan quản lý sản phẩm của mình
    Page<ProductResponseDTO> getMyProducts(Long artisanId, Pageable pageable);
    ProductResponseDTO createProduct(Long artisanId, ProductRequestDTO request);
    ProductResponseDTO updateProduct(Long artisanId, Long productId, ProductRequestDTO request);
    void toggleProductStatus(Long artisanId, Long productId, ProductStatus status);
    void deleteProduct(Long artisanId, Long productId);

    // Admin
    Page<ProductResponseDTO> getAllProductsForAdmin(Pageable pageable, String keyword, ProductStatus status);
    void adminToggleStatus(Long productId, ProductStatus status);
}
