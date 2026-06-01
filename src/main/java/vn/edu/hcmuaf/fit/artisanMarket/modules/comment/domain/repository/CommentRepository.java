package vn.edu.hcmuaf.fit.artisanMarket.modules.comment.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.comment.domain.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    List<Comment> findByUserId(Long userId);
    List<Comment> findByUserUsername(String username);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.product.id = :productId")
    Double calculateAverageRating(@Param("productId") Long productId);
}
