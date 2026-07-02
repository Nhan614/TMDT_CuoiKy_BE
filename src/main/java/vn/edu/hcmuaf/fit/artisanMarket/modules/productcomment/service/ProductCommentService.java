package vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentUpdateDTO;

import java.util.List;

public interface ProductCommentService {

    /** Thêm bình luận mới hoặc reply (khi request.parentId != null). */
    ProductCommentResponseDTO addComment(String username, Long productId, ProductCommentRequestDTO request);

    /** Lấy danh sách bình luận gốc của sản phẩm, có phân trang. */
    Page<ProductCommentResponseDTO> getCommentsByProduct(Long productId, Pageable pageable);

    /** Lấy toàn bộ danh sách reply của một bình luận gốc. */
    List<ProductCommentResponseDTO> getReplies(Long parentId);

    /** Cập nhật nội dung bình luận. Chỉ chủ sở hữu mới được thực hiện. */
    ProductCommentResponseDTO updateComment(String username, Long commentId, ProductCommentUpdateDTO dto);

    /**
     * Xóa bình luận:
     * - Chủ sở hữu hoặc ADMIN.
     * - Bình luận gốc có reply → xóa mềm (đổi nội dung thành "[Bình luận đã bị xóa]").
     * - Bình luận gốc không có reply hoặc là reply → xóa cứng.
     */
    void deleteComment(String username, Long commentId);
}
