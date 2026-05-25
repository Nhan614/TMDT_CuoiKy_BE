package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryRequestDTO {
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private Long parentId;
}
