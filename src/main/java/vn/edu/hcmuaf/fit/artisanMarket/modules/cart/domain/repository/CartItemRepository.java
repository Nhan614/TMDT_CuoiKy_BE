package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
