package vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.productcomment.domain.entity.ProductComment;

import java.util.List;

@Repository
public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {

    /**
     * Lấy danh sách bình luận gốc (parent == null) theo sản phẩm, sắp xếp mới nhất trước.
     */
    Page<ProductComment> findByProductIdAndParentIsNullOrderByCreatedAtDesc(Long productId, Pageable pageable);

    /**
     * Lấy tất cả reply của một bình luận gốc, sắp xếp cũ nhất trước.
     */
    List<ProductComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /**
     * Đếm số lượng reply của một bình luận gốc.
     */
    long countByParentId(Long parentId);
}
