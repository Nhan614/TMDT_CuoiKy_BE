package vn.edu.hcmuaf.fit.artisanMarket.modules.auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);

    // === MỚI ===
    Optional<User> findByVerificationToken(String token);
}