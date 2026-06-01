package vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.domain.entity.Comment;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {
    private Long id;
    private String content;
    private Integer rating;
    private Boolean isPurchased;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;

    public static CommentResponseDTO fromEntity(Comment comment) {
        if (comment == null) return null;
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .rating(comment.getRating())
                .isPurchased(comment.getIsPurchased())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .username(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
