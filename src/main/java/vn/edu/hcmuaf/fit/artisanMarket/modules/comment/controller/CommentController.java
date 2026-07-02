package vn.edu.hcmuaf.fit.artisanMarket.modules.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentEligibilityDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/products/{productId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> addComment(
            @PathVariable Long productId,
            @RequestBody CommentRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CommentResponseDTO response = commentService.addComment(username, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm bình luận thành công", response));
    }

    @GetMapping("/products/{productId}/comments/eligibility")
    public ResponseEntity<ApiResponse<CommentEligibilityDTO>> checkEligibility(
            @PathVariable Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CommentEligibilityDTO result = commentService.checkEligibility(username, productId);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra điều kiện đánh giá thành công", result));
    }

    @GetMapping("/products/{productId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getCommentsByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        Page<CommentResponseDTO> comments = commentService.getCommentsByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bình luận thành công", comments));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.deleteComment(username, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công"));
    }

    @GetMapping("/comments/my")
    public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getMyComments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CommentResponseDTO> comments = commentService.getMyComments(username);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bình luận của tôi thành công", comments));
    }
}

