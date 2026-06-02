package vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long id,
        Long productId,
        String productName,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal subTotal
) {
    public static OrderItemResponseDTO fromEntity(OrderItem item) {
        if (item == null) {
            return null;
        }
        return new OrderItemResponseDTO(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductName(),
                item.getProductPrice(),
                item.getQuantity(),
                item.getSubTotal()
        );
    }
}
