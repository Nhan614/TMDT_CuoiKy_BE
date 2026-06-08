package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.ArtisanApplication;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.enums.ApplicationStatus;

import java.util.Optional;

@Repository
public interface ArtisanApplicationRepository extends JpaRepository<ArtisanApplication, Long> {
    Optional<ArtisanApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);
    Page<ArtisanApplication> findByStatus(ApplicationStatus status, Pageable pageable);
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);
    Optional<ArtisanApplication> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
