package vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto;

import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.model.Category;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;

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
    private String thumbnailUrl;
    private List<String> images;
    private List<String> materials;
    private Long categoryId;
    private String categoryName;
    private Double averageRating;
    private boolean isActive;
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
                .artisanName(product.getArtisanName())
                .thumbnailUrl(product.getThumbnailUrl())
                .images(product.getImages())
                .materials(product.getMaterials())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .averageRating(product.getAverageRating())
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
