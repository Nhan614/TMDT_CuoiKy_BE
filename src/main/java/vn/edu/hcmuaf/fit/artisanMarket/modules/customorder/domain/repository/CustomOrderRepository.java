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

    @org.springframework.data.jpa.repository.Query("SELECT SUM(co.quotedPrice) FROM CustomOrder co WHERE co.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderPaymentStatus.PAID")
    java.math.BigDecimal sumTotalPaidCustomRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT co FROM CustomOrder co WHERE co.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderPaymentStatus.PAID AND co.paymentAt BETWEEN :start AND :end")
    java.util.List<CustomOrder> findPaidCustomOrdersBetweenDates(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end
    );

    @org.springframework.data.jpa.repository.Query("SELECT co.status, COUNT(co) FROM CustomOrder co GROUP BY co.status")
    java.util.List<Object[]> countCustomOrdersByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT co.artisan.name, SUM(co.quotedPrice), COUNT(co.id) " +
            "FROM CustomOrder co " +
            "WHERE co.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderPaymentStatus.PAID " +
            "GROUP BY co.artisan.name " +
            "ORDER BY SUM(co.quotedPrice) DESC")
    java.util.List<Object[]> findTopArtisansFromCustomOrdersRaw(org.springframework.data.domain.Pageable pageable);
}
