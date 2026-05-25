package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto;

import lombok.*;

import lombok.Builder;

@Builder
public record UpdateCartItemRequestDTO(
        Integer quantity
) {}
