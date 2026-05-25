package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.service;

import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.AddToCartRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.CartResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.UpdateCartItemRequestDTO;

public interface CartService {
    CartResponseDTO getCart(String username);
    CartResponseDTO addToCart(String username, AddToCartRequestDTO request);
    CartResponseDTO updateCartItem(String username, Long productId, UpdateCartItemRequestDTO request);
    CartResponseDTO removeCartItem(String username, Long productId);
    CartResponseDTO clearCart(String username);
}
