package vn.edu.hcmuaf.fit.artisanMarket.modules.comment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.exception.ProductNotPurchasedException;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.domain.entity.Comment;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.domain.repository.CommentRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentEligibilityDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.dto.CommentResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.service.CommentService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository.OrderItemRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public CommentResponseDTO addComment(String username, Long productId, CommentRequestDTO request) {
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("Nội dung bình luận không được để trống");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 1 đến 5");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (commentRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        boolean isPurchased = checkIfUserPurchasedProduct(user.getId(), productId);
        if (!isPurchased) {
            throw new ProductNotPurchasedException(
                    "Bạn cần mua và thanh toán sản phẩm này trước khi có thể đánh giá");
        }

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .rating(request.getRating())
                .isPurchased(true)
                .product(product)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        updateProductAverageRating(productId);

        return CommentResponseDTO.fromEntity(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getCommentsByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        Page<Comment> comments = commentRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return comments.map(CommentResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void deleteComment(String username, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!comment.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Không có quyền xóa bình luận này");
        }

        Long productId = comment.getProduct().getId();
        commentRepository.delete(comment);

        updateProductAverageRating(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getMyComments(String username) {
        List<Comment> comments = commentRepository.findByUserUsername(username);
        return comments.stream()
                .map(CommentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentEligibilityDTO checkEligibility(String username, Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        boolean hasPurchased = checkIfUserPurchasedProduct(user.getId(), productId);
        boolean hasReviewed = commentRepository.existsByProductIdAndUserId(productId, user.getId());
        boolean canReview = hasPurchased && !hasReviewed;

        return new CommentEligibilityDTO(hasPurchased, hasReviewed, canReview);
    }

    private void updateProductAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        Double avgRating = commentRepository.calculateAverageRating(productId);
        product.setAverageRating(avgRating != null ? avgRating : 0.0);
        productRepository.save(product);
    }

    private boolean checkIfUserPurchasedProduct(Long userId, Long productId) {
        return orderItemRepository.existsPaidPurchaseByUserAndProduct(userId, productId, PaymentStatus.PAID);
    }
}

