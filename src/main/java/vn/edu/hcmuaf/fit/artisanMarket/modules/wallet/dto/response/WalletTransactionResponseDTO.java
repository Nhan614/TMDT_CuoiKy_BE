package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.WalletTransaction;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionResponseDTO(
    Long id,
    WalletTransactionType type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String description,
    LocalDateTime createdAt,
    Long customOrderId
) {
    public static WalletTransactionResponseDTO fromEntity(WalletTransaction transaction) {
        return new WalletTransactionResponseDTO(
            transaction.getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getBalanceAfter(),
            transaction.getDescription(),
            transaction.getCreatedAt(),
            transaction.getCustomOrder() != null ? transaction.getCustomOrder().getId() : null
        );
    }
}
