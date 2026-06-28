package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.WithdrawalRequest;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WithdrawalStatus;

import java.math.BigDecimal;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    Page<WithdrawalRequest> findByUserId(Long userId, Pageable pageable);
    Page<WithdrawalRequest> findByStatus(WithdrawalStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRequest w WHERE w.user.id = :userId AND w.status = 'PENDING'")
    BigDecimal sumPendingWithdrawalAmountByUserId(@Param("userId") Long userId);
}
