package vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto;

import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private boolean isPreOrder;
    private Integer makingDays;
    private String artisanName;
    private Long artisanId;
    private String thumbnailUrl;
    private String imageUrl; // Cho FE sử dụng đồng bộ
    private String cloudinaryPublicId;
    private List<String> images;
    private List<String> materials;
    private Long categoryId;
    private String categoryName;
    private Double averageRating;
    private boolean isActive;
    private ProductStatus status;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponseDTO fromEntity(Product product) {
        if (product == null)
            return null;
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .stockQuantity(product.getStockQuantity())
                .isPreOrder(product.isPreOrder())
                .makingDays(product.getMakingDays())
                .artisanName(product.getArtisan() != null ? product.getArtisan().getName() : product.getArtisanName())
                .artisanId(product.getArtisan() != null ? product.getArtisan().getId() : null)
                .thumbnailUrl(product.getThumbnailUrl())
                .imageUrl(product.getThumbnailUrl())
                .cloudinaryPublicId(product.getCloudinaryPublicId())
                .images(product.getImages())
                .materials(product.getMaterials())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .averageRating(product.getAverageRating())
                .isActive(product.isActive())
                .status(product.getStatus())
                .rejectReason(product.getRejectReason())
                .reviewedBy(product.getReviewedBy())
                .reviewedAt(product.getReviewedAt())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
