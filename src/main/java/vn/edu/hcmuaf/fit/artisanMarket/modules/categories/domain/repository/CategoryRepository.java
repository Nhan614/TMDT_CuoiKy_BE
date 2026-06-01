package vn.edu.hcmuaf.fit.artisanMarket.modules.categories.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.hcmuaf.fit.artisanMarket.modules.categories.model.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

        Optional<Category> findBySlug(String slug);

        boolean existsBySlug(String slug);

        boolean existsBySlugAndIdNot(String slug, Long id);

        boolean existsByName(String name);

        boolean existsByNameAndIdNot(String name, Long id);

        @Query("SELECT c FROM Category c WHERE " +
                        "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
                        +
                        "(:parentId IS NULL OR (c.parent IS NOT NULL AND c.parent.id = :parentId)) AND " +
                        "(:isActive IS NULL OR c.isActive = :isActive)")
        Page<Category> findCategories(
                        @Param("search") String search,
                        @Param("parentId") Long parentId,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);
}
