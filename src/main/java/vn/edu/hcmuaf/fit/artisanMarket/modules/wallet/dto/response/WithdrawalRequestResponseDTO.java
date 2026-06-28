package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.WithdrawalRequest;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalRequestResponseDTO(
    Long id,
    Long userId,
    String username,
    BigDecimal amount,
    String bankName,
    String accountNumber,
    String accountHolder,
    WithdrawalStatus status,
    String note,
    Long reviewedBy,
    String reviewerName,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static WithdrawalRequestResponseDTO fromEntity(WithdrawalRequest request) {
        return new WithdrawalRequestResponseDTO(
            request.getId(),
            request.getUser().getId(),
            request.getUser().getUsername(),
            request.getAmount(),
            request.getBankName(),
            request.getAccountNumber(),
            request.getAccountHolder(),
            request.getStatus(),
            request.getNote(),
            request.getReviewedBy() != null ? request.getReviewedBy().getId() : null,
            request.getReviewedBy() != null ? request.getReviewedBy().getUsername() : null,
            request.getReviewedAt(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
