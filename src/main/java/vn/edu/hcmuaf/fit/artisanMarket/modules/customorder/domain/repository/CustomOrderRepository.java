package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.CustomOrder;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;

@Repository
public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {

    long countByUserIdAndArtisanIdAndStatus(Long userId, Long artisanId, CustomOrderStatus status);

    Page<CustomOrder> findByUserId(Long userId, Pageable pageable);
    Page<CustomOrder> findByUserIdAndStatus(Long userId, CustomOrderStatus status, Pageable pageable);

    Page<CustomOrder> findByArtisanId(Long artisanId, Pageable pageable);
    Page<CustomOrder> findByArtisanIdAndStatus(Long artisanId, CustomOrderStatus status, Pageable pageable);
}
