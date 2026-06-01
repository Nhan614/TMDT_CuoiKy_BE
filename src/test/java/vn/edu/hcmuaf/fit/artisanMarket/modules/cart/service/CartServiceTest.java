package vn.edu.hcmuaf.fit.artisanMarket.modules.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.Cart;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.CartItem;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.repository.CartRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.AddToCartRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.CartResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.dto.UpdateCartItemRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.service.impl.CartServiceImpl;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@gmail.com")
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(100L)
                .name("Sản phẩm Handmade")
                .slug("san-pham-handmade")
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(10)
                .isActive(true)
                .isPreOrder(false)
                .build();
    }

    @Test
    void getCart_ExistingCart_ReturnsCartResponse() {
        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.of(cart));

        CartResponseDTO response = cartService.getCart("testuser");

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(0, response.items().size());
        verify(cartRepository, times(1)).findByUserUsername("testuser");
    }

    @Test
    void getCart_NewCart_CreatesAndReturnsCartResponse() {
        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDTO response = cartService.getCart("testuser");

        assertNotNull(response);
        verify(cartRepository, times(1)).findByUserUsername("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addToCart_Success() {
        AddToCartRequestDTO request = AddToCartRequestDTO.builder()
                .productId(100L)
                .quantity(2)
                .build();

        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.addToCart("testuser", request);

        assertNotNull(response);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addToCart_ExceedStockQuantity_ThrowsException() {
        AddToCartRequestDTO request = AddToCartRequestDTO.builder()
                .productId(100L)
                .quantity(15) // exceeds stockQuantity of 10
                .build();

        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart("testuser", request);
        });

        assertTrue(exception.getMessage().contains("vượt quá số lượng trong kho"));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_Success() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();
        cart.getItems().add(cartItem);

        UpdateCartItemRequestDTO request = UpdateCartItemRequestDTO.builder()
                .quantity(5)
                .build();

        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.updateCartItem("testuser", 100L, request);

        assertNotNull(response);
        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void removeCartItem_Success() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.removeCartItem("testuser", 100L);

        assertNotNull(response);
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void clearCart_Success() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserUsername("testuser")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.clearCart("testuser");

        assertNotNull(response);
        assertEquals(0, cart.getItems().size());
    }
}
