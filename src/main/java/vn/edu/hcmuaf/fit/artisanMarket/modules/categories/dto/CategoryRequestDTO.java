package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {

    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String name;

    @Size(max = 100, message = "Slug không được vượt quá 100 ký tự")
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang"
    )
    private String slug; // Có thể null/blank — service sẽ tự sinh từ name

    private String description;

    private String imageUrl;

    private Boolean isActive;

    private Long parentId;
}
