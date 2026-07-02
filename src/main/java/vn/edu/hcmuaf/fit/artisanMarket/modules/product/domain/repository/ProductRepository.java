package vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        Optional<Product> findBySlug(String slug);

        boolean existsBySlug(String slug);

        boolean existsBySlugAndIdNot(String slug, Long id);

        Page<Product> findByArtisanIdAndStatusNot(Long artisanId, ProductStatus status, Pageable pageable);

        Optional<Product> findByIdAndArtisanId(Long id, Long artisanId);

        @Query("SELECT p FROM Product p WHERE " +
                        "p.status = vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus.ACTIVE AND " +
                        "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
                        +
                        "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                        "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
                        "(:isActive IS NULL OR p.isActive = :isActive)")
        Page<Product> findProducts(
                        @Param("search") String search,
                        @Param("categoryId") Long categoryId,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        Page<Product> findByStatus(ProductStatus status, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE " +
                        "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:status IS NULL OR p.status = :status)")
        Page<Product> findAllForAdmin(
                        @Param("keyword") String keyword,
                        @Param("status") ProductStatus status,
                        Pageable pageable);

        @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.enums.ProductStatus.ACTIVE AND p.isActive = true")
        long countActiveProductsByCategoryId(@Param("categoryId") Long categoryId);
}
