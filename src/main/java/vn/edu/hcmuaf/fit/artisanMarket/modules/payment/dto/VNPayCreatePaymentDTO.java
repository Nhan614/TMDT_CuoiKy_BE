package vn.edu.hcmuaf.fit.artisanMarket.modules.payment.dto;

import jakarta.validation.constraints.NotNull;

public record VNPayCreatePaymentDTO(
        @NotNull(message = "Mã đơn hàng không được rỗng")
        Long orderId
) {}
