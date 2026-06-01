package vn.edu.hcmuaf.fit.artisanMarket.modules.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.domain.repository.CategoryRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.model.Category;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto.ProductResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private ProductResponseDTO toDTO(Product product) {
        return ProductResponseDTO.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Đường dẫn thân thiện (slug) của sản phẩm đã tồn tại");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .isPreOrder(request.getIsPreOrder() != null ? request.getIsPreOrder() : false)
                .makingDays(request.getMakingDays())
                .artisanName(request.getArtisanName())
                .thumbnailUrl(request.getThumbnailUrl())
                .images(request.getImages())
                .materials(request.getMaterials())
                .category(category)
                .averageRating(0.0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Product savedProduct = productRepository.save(product);
        return toDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        return toDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(int page, int size, String search, Long categoryId, Boolean isActive,
            String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "rating":
                    sort = Sort.by(Sort.Direction.DESC, "averageRating");
                    break;
                case "newest":
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size, sort);
        Page<Product> products = productRepository.findProducts(search, categoryId, isActive, pageable);
        return products.map(this::toDTO);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
            if (productRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new RuntimeException("Đường dẫn thân thiện (slug) của sản phẩm đã tồn tại");
            }
            product.setSlug(request.getSlug());
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            product.setShortDescription(request.getShortDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDiscountPrice() != null) {
            product.setDiscountPrice(request.getDiscountPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getIsPreOrder() != null) {
            product.setPreOrder(request.getIsPreOrder());
        }
        if (request.getMakingDays() != null) {
            product.setMakingDays(request.getMakingDays());
        }
        if (request.getArtisanName() != null) {
            product.setArtisanName(request.getArtisanName());
        }
        if (request.getThumbnailUrl() != null) {
            product.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getMaterials() != null) {
            product.setMaterials(request.getMaterials());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            product.setCategory(category);
        }
        if (request.getIsActive() != null) {
            product.setActive(request.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);
        return toDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        productRepository.delete(product);
    }
}
