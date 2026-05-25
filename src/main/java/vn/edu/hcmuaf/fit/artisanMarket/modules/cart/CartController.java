package vn.edu.hcmuaf.fit.artisanMarket.modules.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.AddToCartRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.CartResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.UpdateCartItemRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy giỏ hàng thành công",
                cartService.getCart(username)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addToCart(@RequestBody AddToCartRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(
                "Thêm sản phẩm vào giỏ hàng thành công",
                cartService.addToCart(username, request)));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateCartItem(
            @PathVariable Long productId,
            @RequestBody UpdateCartItemRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật số lượng sản phẩm thành công",
                cartService.updateCartItem(username, productId, request)));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeCartItem(@PathVariable Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa sản phẩm khỏi giỏ hàng thành công",
                cartService.removeCartItem(username, productId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> clearCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa sạch giỏ hàng thành công",
                cartService.clearCart(username)));
    }
}
