package vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.Order;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.OrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentMethod;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String orderCode,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        String shippingAddress,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponseDTO> items
) {
    public static OrderResponseDTO fromEntity(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDTO> itemDTOs = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponseDTO::fromEntity).toList()
                : Collections.emptyList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getNote(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemDTOs
        );
    }
}
