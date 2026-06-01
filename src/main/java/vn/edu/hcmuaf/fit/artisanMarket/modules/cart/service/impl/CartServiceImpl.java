package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.Cart;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.CartItem;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.repository.CartRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.AddToCartRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.CartResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.UpdateCartItemRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.service.CartService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponseDTO getCart(String username) {
        Cart cart = getOrCreateCart(username);
        return CartResponseDTO.fromEntity(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO addToCart(String username, AddToCartRequestDTO request) {
        Cart cart = getOrCreateCart(username);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (!product.isActive()) {
            throw new RuntimeException("Sản phẩm hiện không hoạt động");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int addQty = request.quantity() != null ? request.quantity() : 1;
        if (addQty <= 0) {
            throw new RuntimeException("Số lượng sản phẩm thêm vào phải lớn hơn 0");
        }

        int finalQty = addQty;
        if (existingItem != null) {
            finalQty += existingItem.getQuantity();
        }

        // Validate stock
        if (!product.isPreOrder() && finalQty > product.getStockQuantity()) {
            throw new RuntimeException(
                    "Số lượng sản phẩm vượt quá số lượng trong kho (" + product.getStockQuantity() + ")");
        }

        if (existingItem != null) {
            existingItem.setQuantity(finalQty);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(finalQty)
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return CartResponseDTO.fromEntity(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateCartItem(String username, Long productId, UpdateCartItemRequestDTO request) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        int newQty = request.quantity();
        if (newQty <= 0) {
            cart.getItems().remove(existingItem);
        } else {
            Product product = existingItem.getProduct();
            if (!product.isPreOrder() && newQty > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Số lượng sản phẩm vượt quá số lượng trong kho (" + product.getStockQuantity() + ")");
            }
            existingItem.setQuantity(newQty);
        }

        Cart savedCart = cartRepository.save(cart);
        return CartResponseDTO.fromEntity(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeCartItem(String username, Long productId) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        cart.getItems().remove(existingItem);
        Cart savedCart = cartRepository.save(cart);
        return CartResponseDTO.fromEntity(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO clearCart(String username) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        cart.getItems().clear();
        Cart savedCart = cartRepository.save(cart);
        return CartResponseDTO.fromEntity(savedCart);
    }

    private Cart getOrCreateCart(String username) {
        return cartRepository.findByUserUsername(username)
                .orElseGet(() -> {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
