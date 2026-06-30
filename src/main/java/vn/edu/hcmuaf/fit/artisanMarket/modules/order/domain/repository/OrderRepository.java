package vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserUsernameOrderByCreatedAtDesc(String username);
    Optional<Order> findByOrderCode(String orderCode);
    boolean existsByOrderCode(String orderCode);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus.PAID")
    java.math.BigDecimal sumTotalPaidRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus.PAID AND o.createdAt BETWEEN :start AND :end")
    List<Order> findPaidOrdersBetweenDates(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end
    );

    @org.springframework.data.jpa.repository.Query("SELECT oi.product.id, oi.productName, oi.product.thumbnailUrl, SUM(oi.quantity), SUM(oi.subTotal) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus.PAID " +
            "GROUP BY oi.product.id, oi.productName, oi.product.thumbnailUrl " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsRaw(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT oi.product.artisanName, SUM(oi.subTotal), COUNT(DISTINCT oi.order.id) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.paymentStatus = vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus.PAID " +
            "GROUP BY oi.product.artisanName " +
            "ORDER BY SUM(oi.subTotal) DESC")
    List<Object[]> findTopArtisansFromRegularOrdersRaw(org.springframework.data.domain.Pageable pageable);
}
