package vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT p FROM Product p WHERE " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> findProducts(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
