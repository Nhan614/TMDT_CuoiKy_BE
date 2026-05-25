package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto;

import lombok.Builder;

@Builder
public record AddToCartRequestDTO(
        Long productId,
        Integer quantity
) {
    public AddToCartRequestDTO {
        if (quantity == null) {
            quantity = 1;
        }
    }
}
