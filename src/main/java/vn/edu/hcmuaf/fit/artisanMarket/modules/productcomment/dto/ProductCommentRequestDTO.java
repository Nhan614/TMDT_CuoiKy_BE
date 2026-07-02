package vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCommentRequestDTO {
    private String content;
    /** Null = bình luận gốc mới. Non-null = reply cho bình luận có ID này. */
    private Long parentId;
}
