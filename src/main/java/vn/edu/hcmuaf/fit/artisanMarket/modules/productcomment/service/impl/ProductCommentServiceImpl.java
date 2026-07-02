package vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.domain.entity.ProductComment;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.domain.repository.ProductCommentRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.dto.ProductCommentUpdateDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.service.ProductCommentService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/** Nội dung thay thế khi bình luận gốc bị xóa mềm. */
@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl implements ProductCommentService {

    private static final String DELETED_CONTENT = "[Bình luận đã bị xóa]";

    private final ProductCommentRepository productCommentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // -----------------------------------------------------------------------
    // addComment
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public ProductCommentResponseDTO addComment(String username, Long productId, ProductCommentRequestDTO request) {
        // --- Validate content ---
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("Nội dung bình luận không được để trống");
        }
        if (request.getContent().trim().length() > 1000) {
            throw new RuntimeException("Nội dung bình luận không được vượt quá 1000 ký tự");
        }

        // --- Kiểm tra sản phẩm ---
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // --- Kiểm tra người dùng ---
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // --- Xử lý parent (nếu là reply) ---
        ProductComment parent = null;
        if (request.getParentId() != null) {
            parent = productCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Bình luận gốc không tồn tại"));

            // Đảm bảo bình luận cha thuộc đúng sản phẩm
            if (!parent.getProduct().getId().equals(productId)) {
                throw new RuntimeException("Bình luận gốc không thuộc sản phẩm này");
            }

            // Chặn reply của reply (đảm bảo chỉ 1 cấp)
            if (parent.getParent() != null) {
                throw new RuntimeException("Không thể trả lời một bình luận trả lời");
            }

            // Chặn reply vào bình luận đã bị xóa mềm
            if (DELETED_CONTENT.equals(parent.getContent())) {
                throw new RuntimeException("Không thể trả lời bình luận đã bị xóa");
            }
        }

        // --- Lưu bình luận mới ---
        ProductComment comment = ProductComment.builder()
                .content(request.getContent().trim())
                .product(product)
                .user(user)
                .parent(parent)
                .build();

        ProductComment saved = productCommentRepository.save(comment);

        long replyCount = (parent == null) ? productCommentRepository.countByParentId(saved.getId()) : 0L;
        return ProductCommentResponseDTO.fromEntity(saved, replyCount);
    }

    // -----------------------------------------------------------------------
    // getCommentsByProduct
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCommentResponseDTO> getCommentsByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        Page<ProductComment> page = productCommentRepository
                .findByProductIdAndParentIsNullOrderByCreatedAtDesc(productId, pageable);

        return page.map(comment -> {
            long replyCount = productCommentRepository.countByParentId(comment.getId());
            return ProductCommentResponseDTO.fromEntity(comment, replyCount);
        });
    }

    // -----------------------------------------------------------------------
    // getReplies
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<ProductCommentResponseDTO> getReplies(Long parentId) {
        ProductComment parent = productCommentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        // Parent phải là bình luận gốc
        if (parent.getParent() != null) {
            throw new RuntimeException("Bình luận này không phải bình luận gốc");
        }

        return productCommentRepository.findByParentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(reply -> ProductCommentResponseDTO.fromEntity(reply, 0L))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // updateComment
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public ProductCommentResponseDTO updateComment(String username, Long commentId, ProductCommentUpdateDTO dto) {
        ProductComment comment = productCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Chỉ chủ sở hữu được sửa
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        // Không được sửa bình luận đã bị xóa mềm
        if (DELETED_CONTENT.equals(comment.getContent())) {
            throw new RuntimeException("Không thể chỉnh sửa bình luận đã bị xóa");
        }

        // Validate nội dung mới
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new RuntimeException("Nội dung bình luận không được để trống");
        }
        if (dto.getContent().trim().length() > 1000) {
            throw new RuntimeException("Nội dung bình luận không được vượt quá 1000 ký tự");
        }

        comment.setContent(dto.getContent().trim());
        // updatedAt sẽ tự cập nhật qua @PreUpdate
        ProductComment saved = productCommentRepository.save(comment);

        long replyCount = (comment.getParent() == null)
                ? productCommentRepository.countByParentId(saved.getId())
                : 0L;
        return ProductCommentResponseDTO.fromEntity(saved, replyCount);
    }

    // -----------------------------------------------------------------------
    // deleteComment
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteComment(String username, Long commentId) {
        ProductComment comment = productCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Chỉ chủ sở hữu hoặc ADMIN được xóa
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này");
        }

        boolean isRootComment = (comment.getParent() == null);

        if (isRootComment) {
            long replyCount = productCommentRepository.countByParentId(commentId);
            if (replyCount > 0) {
                // Xóa mềm: giữ nguyên dòng comment để thread reply không bị vỡ
                comment.setContent(DELETED_CONTENT);
                productCommentRepository.save(comment);
            } else {
                // Không có reply → xóa cứng
                productCommentRepository.delete(comment);
            }
        } else {
            // Reply → xóa cứng trực tiếp
            productCommentRepository.delete(comment);
        }
    }
}
