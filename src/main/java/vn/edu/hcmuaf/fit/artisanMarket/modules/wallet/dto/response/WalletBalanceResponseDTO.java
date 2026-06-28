package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response;

import java.math.BigDecimal;

public record WalletBalanceResponseDTO(
    BigDecimal balance,
    String currency
) {}
