package vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductRequestDTO {
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private Boolean isPreOrder;
    private Integer makingDays;
    private String artisanName;
    private String thumbnailUrl;
    private List<String> images;
    private List<String> materials;
    private Long categoryId;
    private Boolean isActive;
    
    // File ảnh chính của sản phẩm (dùng khi upload/update ảnh từ form)
    private MultipartFile image;
}
