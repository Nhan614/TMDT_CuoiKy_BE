package vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.domain.entity.ProductComment;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCommentResponseDTO {

    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String fullName;
    /** avatarUrl luôn null vì User entity chưa có trường này; frontend dùng initials thay thế */
    private String avatarUrl;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** True khi updatedAt > createdAt, nghĩa là đã chỉnh sửa ít nhất một lần */
    private boolean isEdited;
    private long replyCount;

    /**
     * Chuyển đổi entity sang DTO.
     *
     * @param comment    thực thể ProductComment
     * @param replyCount số lượng reply (chỉ có ý nghĩa với bình luận gốc)
     */
    public static ProductCommentResponseDTO fromEntity(ProductComment comment, long replyCount) {
        if (comment == null) return null;

        boolean edited = comment.getUpdatedAt() != null
                && comment.getCreatedAt() != null
                && comment.getUpdatedAt().isAfter(comment.getCreatedAt().plusSeconds(1));

        return ProductCommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .username(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .fullName(comment.getUser() != null ? comment.getUser().getFullName() : null)
                .avatarUrl(null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isEdited(edited)
                .replyCount(replyCount)
                .build();
    }
}
