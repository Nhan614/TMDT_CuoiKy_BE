package vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentUpdateDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.service.ProductCommentService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductCommentController {

    private final ProductCommentService productCommentService;

    /**
     * POST /api/products/{productId}/product-comments
     * Tạo bình luận mới hoặc reply (parentId trong body).
     */
    @PostMapping("/products/{productId}/product-comments")
    public ResponseEntity<ApiResponse<ProductCommentResponseDTO>> addComment(
            @PathVariable Long productId,
            @RequestBody ProductCommentRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ProductCommentResponseDTO response = productCommentService.addComment(username, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm bình luận thành công", response));
    }

    /**
     * GET /api/products/{productId}/product-comments?page=&size=
     * Danh sách bình luận gốc của sản phẩm, có phân trang.
     */
    @GetMapping("/products/{productId}/product-comments")
    public ResponseEntity<ApiResponse<List<ProductCommentResponseDTO>>> getCommentsByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        Page<ProductCommentResponseDTO> comments = productCommentService.getCommentsByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bình luận thành công", comments));
    }

    /**
     * GET /api/product-comments/{id}/replies
     * Danh sách reply của một bình luận gốc (tải khi user bấm "Xem trả lời").
     */
    @GetMapping("/product-comments/{id}/replies")
    public ResponseEntity<ApiResponse<List<ProductCommentResponseDTO>>> getReplies(
            @PathVariable Long id) {
        List<ProductCommentResponseDTO> replies = productCommentService.getReplies(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trả lời thành công", replies));
    }

    /**
     * PUT /api/product-comments/{id}
     * Sửa nội dung bình luận (chỉ chủ sở hữu).
     */
    @PutMapping("/product-comments/{id}")
    public ResponseEntity<ApiResponse<ProductCommentResponseDTO>> updateComment(
            @PathVariable Long id,
            @RequestBody ProductCommentUpdateDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ProductCommentResponseDTO response = productCommentService.updateComment(username, id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bình luận thành công", response));
    }

    /**
     * DELETE /api/product-comments/{id}
     * Xóa bình luận (chủ sở hữu hoặc ADMIN).
     */
    @DeleteMapping("/product-comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        productCommentService.deleteComment(username, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công"));
    }
}
