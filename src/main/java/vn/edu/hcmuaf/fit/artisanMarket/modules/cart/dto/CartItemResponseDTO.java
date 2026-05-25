package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto;

import lombok.Builder;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.CartItem;

import java.math.BigDecimal;

@Builder
public record CartItemResponseDTO(
        Long id,
        Long productId,
        String productName,
        String productSlug,
        BigDecimal productPrice,
        String productThumbnailUrl,
        Integer quantity,
        Integer stockQuantity,
        boolean isPreOrder,
        BigDecimal subTotal
) {
    public static CartItemResponseDTO fromEntity(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        var product = cartItem.getProduct();

        // Xác định giá bán (ưu tiên giá giảm)
        BigDecimal price = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getPrice();

        // Tính thành tiền
        BigDecimal subTotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponseDTO.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .productPrice(price)
                .productThumbnailUrl(product.getThumbnailUrl())
                .quantity(cartItem.getQuantity())
                .stockQuantity(product.getStockQuantity())
                .isPreOrder(product.isPreOrder())
                .subTotal(subTotal)
                .build();
    }
}
