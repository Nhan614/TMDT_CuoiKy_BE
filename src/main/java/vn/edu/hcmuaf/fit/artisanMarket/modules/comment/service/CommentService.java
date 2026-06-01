package vn.edu.hcmuaf.fit.artisanMarket.modules.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO addComment(String username, Long productId, CommentRequestDTO request);
    Page<CommentResponseDTO> getCommentsByProduct(Long productId, Pageable pageable);
    void deleteComment(String username, Long commentId);
    List<CommentResponseDTO> getMyComments(String username);
}
