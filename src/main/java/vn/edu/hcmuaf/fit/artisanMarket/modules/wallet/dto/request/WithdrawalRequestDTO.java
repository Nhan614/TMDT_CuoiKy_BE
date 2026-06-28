package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawalRequestDTO(
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000.00", message = "Số tiền rút tối thiểu là 1,000 VNĐ")
    BigDecimal amount,

    @NotBlank(message = "Tên ngân hàng không được để trống")
    String bankName,

    @NotBlank(message = "Số tài khoản không được để trống")
    String accountNumber,

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    String accountHolder
) {}
