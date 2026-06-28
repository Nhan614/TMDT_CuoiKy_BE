package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReviewWithdrawalRequestDTO(
    @NotBlank(message = "Hành động không được để trống")
    @Pattern(regexp = "APPROVE|REJECT", message = "Hành động chỉ có thể là APPROVE hoặc REJECT")
    String action,

    String note
) {}
