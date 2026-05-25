package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserUsername(String username);
}
