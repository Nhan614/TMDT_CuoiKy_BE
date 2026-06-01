package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto;

import lombok.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.model.Category;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private boolean isActive;
    private Long parentId;
    private String parentName;

    public static CategoryResponseDTO fromEntity(Category category) {
        if (category == null)
            return null;
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .isActive(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .build();
    }
}
