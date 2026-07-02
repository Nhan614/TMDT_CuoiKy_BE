package vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.OrderItem;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "WHERE oi.product.id = :productId " +
           "AND oi.order.user.id = :userId " +
           "AND oi.order.paymentStatus = :paymentStatus")
    boolean existsPaidPurchaseByUserAndProduct(@Param("userId") Long userId,
                                               @Param("productId") Long productId,
                                               @Param("paymentStatus") PaymentStatus paymentStatus);
}

