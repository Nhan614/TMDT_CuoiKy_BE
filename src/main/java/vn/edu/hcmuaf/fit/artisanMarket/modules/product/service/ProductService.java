package vn.edu.hcmuaf.fit.artisanMarket.modules.product.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductResponseDTO;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);
    ProductResponseDTO getProductById(Long id);
    Page<ProductResponseDTO> getAllProducts(int page, int size, String search, Long categoryId, Boolean isActive, String sortBy);
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);
    void deleteProduct(Long id);
}
