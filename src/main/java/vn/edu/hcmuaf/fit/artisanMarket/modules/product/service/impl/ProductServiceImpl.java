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
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.service.ProductService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.cloudinary.CloudinaryService;
import vn.edu.hcmuaf.fit.artisanMarket.infrastructure.mail.EmailService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.auth.domain.repository.AuthRepository;
import java.time.LocalDateTime;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ArtisanRepository artisanRepository;
    private final CloudinaryService cloudinaryService;
    private final AuthRepository userRepository;
    private final EmailService emailService;

    private ProductResponseDTO toDTO(Product product) {
        return ProductResponseDTO.fromEntity(product);
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

    // ── ARTISAN PRODUCT MANAGEMENT ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getMyProducts(Long artisanId, Pageable pageable) {
        return productRepository.findByArtisanIdAndStatusNot(artisanId, ProductStatus.DELETED, pageable)
                .map(ProductResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(Long artisanId, ProductRequestDTO request) {
        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nghệ nhân"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getName());
        }

        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        String imageUrl = null;
        String publicId = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            Map<String, String> uploadResult = cloudinaryService.upload(
                    request.getImage(), "artisan-market/products"
            );
            imageUrl = uploadResult.get("url");
            publicId = uploadResult.get("public_id");
        } else if (request.getThumbnailUrl() != null) {
            imageUrl = request.getThumbnailUrl();
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .isPreOrder(request.getIsPreOrder() != null ? request.getIsPreOrder() : false)
                .makingDays(request.getMakingDays())
                .artisanName(artisan.getName())
                .thumbnailUrl(imageUrl)
                .cloudinaryPublicId(publicId)
                .images(request.getImages())
                .materials(request.getMaterials())
                .category(category)
                .artisan(artisan)
                .averageRating(0.0)
                .isActive(false)
                .status(ProductStatus.PENDING)
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponseDTO.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long artisanId, Long productId, ProductRequestDTO request) {
        Product product = productRepository.findByIdAndArtisanId(productId, artisanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền chỉnh sửa"));

        if (request.getSlug() != null && !request.getSlug().trim().isEmpty() && !request.getSlug().equals(product.getSlug())) {
            if (productRepository.existsBySlugAndIdNot(request.getSlug(), productId)) {
                throw new RuntimeException("Đường dẫn thân thiện (slug) của sản phẩm đã tồn tại");
            }
            product.setSlug(request.getSlug());
        } else if (request.getName() != null && !request.getName().equals(product.getName())) {
            String newSlug = generateSlug(request.getName());
            if (productRepository.existsBySlugAndIdNot(newSlug, productId)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            product.setSlug(newSlug);
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (product.getCloudinaryPublicId() != null) {
                try {
                    cloudinaryService.delete(product.getCloudinaryPublicId());
                } catch (Exception e) {
                    // Ignore deletion error
                }
            }
            Map<String, String> uploadResult = cloudinaryService.upload(
                    request.getImage(), "artisan-market/products"
            );
            product.setThumbnailUrl(uploadResult.get("url"));
            product.setCloudinaryPublicId(uploadResult.get("public_id"));
        } else if (request.getThumbnailUrl() != null) {
            product.setThumbnailUrl(request.getThumbnailUrl());
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
        // Khi cập nhật sản phẩm, đưa về trạng thái chờ duyệt PENDING và tạm ẩn
        product.setStatus(ProductStatus.PENDING);
        product.setActive(false);
        product.setRejectReason(null);

        Product updatedProduct = productRepository.save(product);
        return ProductResponseDTO.fromEntity(updatedProduct);
    }

    @Override
    @Transactional
    public void toggleProductStatus(Long artisanId, Long productId, ProductStatus status) {
        Product product = productRepository.findByIdAndArtisanId(productId, artisanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền chỉnh sửa"));
        if (product.getStatus() != ProductStatus.ACTIVE && product.getStatus() != ProductStatus.HIDDEN) {
            throw new RuntimeException("Chỉ có thể ẩn/hiện sản phẩm đã qua kiểm duyệt");
        }
        if (status != ProductStatus.ACTIVE && status != ProductStatus.HIDDEN) {
            throw new RuntimeException("Trạng thái chuyển đổi không hợp lệ");
        }
        product.setStatus(status);
        product.setActive(status == ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long artisanId, Long productId) {
        Product product = productRepository.findByIdAndArtisanId(productId, artisanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền chỉnh sửa"));
        
        product.setStatus(ProductStatus.DELETED);
        product.setActive(false);
        productRepository.save(product);
        
        if (product.getCloudinaryPublicId() != null) {
            try {
                cloudinaryService.delete(product.getCloudinaryPublicId());
                product.setCloudinaryPublicId(null);
                product.setThumbnailUrl(null);
                productRepository.save(product);
            } catch (Exception e) {
                // Log and ignore to prevent failure
            }
        }
    }

    // ── ADMIN PRODUCT MANAGEMENT ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProductsForAdmin(Pageable pageable, String keyword, ProductStatus status) {
        return productRepository.findAllForAdmin(keyword, status, pageable)
                .map(ProductResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void adminToggleStatus(Long productId, ProductStatus status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        if (product.getStatus() != ProductStatus.ACTIVE && product.getStatus() != ProductStatus.HIDDEN) {
            throw new RuntimeException("Chỉ có thể ẩn/hiện sản phẩm đã qua kiểm duyệt");
        }
        if (status != ProductStatus.ACTIVE && status != ProductStatus.HIDDEN) {
            throw new RuntimeException("Trạng thái chuyển đổi không hợp lệ");
        }
        product.setStatus(status);
        product.setActive(status == ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getPendingProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.PENDING, pageable)
                .map(ProductResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public ProductResponseDTO approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalStateException("Sản phẩm không ở trạng thái chờ duyệt");
        }

        String adminUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin"));

        product.setStatus(ProductStatus.ACTIVE);
        product.setActive(true);
        product.setReviewedBy(admin.getId());
        product.setReviewedAt(LocalDateTime.now());
        product.setRejectReason(null);

        Product savedProduct = productRepository.save(product);

        // Gửi email thông báo cho Artisan
        if (product.getArtisan() != null && product.getArtisan().getUserId() != null) {
            userRepository.findById(product.getArtisan().getUserId()).ifPresent(user -> {
                if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                    emailService.sendProductApprovedEmail(user.getEmail(), product.getName());
                }
            });
        }

        return ProductResponseDTO.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO rejectProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalStateException("Sản phẩm không ở trạng thái chờ duyệt");
        }

        String adminUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin"));

        product.setStatus(ProductStatus.REJECTED);
        product.setActive(false);
        product.setRejectReason(reason);
        product.setReviewedBy(admin.getId());
        product.setReviewedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Gửi email thông báo cho Artisan
        if (product.getArtisan() != null && product.getArtisan().getUserId() != null) {
            userRepository.findById(product.getArtisan().getUserId()).ifPresent(user -> {
                if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                    emailService.sendProductRejectedEmail(user.getEmail(), product.getName(), reason);
                }
            });
        }

        return ProductResponseDTO.fromEntity(savedProduct);
    }

    // ── SLUG GENERATOR HELPER ────────────────────────────────────────────────

    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        String slug = name.toLowerCase()
                .replaceAll("á|à|ả|ã|ạ|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ần|ẩ|ẫ|ậ", "a")
                .replaceAll("é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ", "e")
                .replaceAll("í|ì|ỉ|ĩ|ị", "i")
                .replaceAll("ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ", "o")
                .replaceAll("ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự", "u")
                .replaceAll("ý|ỳ|ỷ|ỹ|ỵ", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        return slug;
    }
}
