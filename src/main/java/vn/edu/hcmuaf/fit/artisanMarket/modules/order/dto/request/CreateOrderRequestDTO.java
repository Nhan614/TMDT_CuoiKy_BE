package vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentMethod;

public record CreateOrderRequestDTO(
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String shippingAddress,

        String note,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod paymentMethod
) {}
