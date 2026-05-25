package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto;

import lombok.Builder;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.Cart;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Builder
public record CartResponseDTO(
        Long id,
        List<CartItemResponseDTO> items,
        BigDecimal totalPrice,
        Integer totalItems
) {
    public static CartResponseDTO fromEntity(Cart cart) {
        if (cart == null) {
            return null;
        }

        // Mapping danh sách item
        List<CartItemResponseDTO> itemDTOs = cart.getItems() != null
                ? cart.getItems().stream()
                .map(CartItemResponseDTO::fromEntity)
                .toList() // Java 16+ dùng .toList() cho gọn thay vì Collectors.toList()
                : Collections.emptyList();

        // Tính toán tổng tiền từ danh sách DTO
        BigDecimal totalPrice = itemDTOs.stream()
                .map(CartItemResponseDTO::subTotal) // Record gọi thẳng subTotal(), không có "get"
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính tổng số lượng item
        int totalItems = itemDTOs.stream()
                .mapToInt(CartItemResponseDTO::quantity) // Record gọi quantity()
                .sum();

        return CartResponseDTO.builder()
                .id(cart.getId())
                .items(itemDTOs)
                .totalPrice(totalPrice)
                .totalItems(totalItems)
                .build();
    }
}
