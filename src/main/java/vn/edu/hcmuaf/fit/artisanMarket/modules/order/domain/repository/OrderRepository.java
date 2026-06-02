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
}
