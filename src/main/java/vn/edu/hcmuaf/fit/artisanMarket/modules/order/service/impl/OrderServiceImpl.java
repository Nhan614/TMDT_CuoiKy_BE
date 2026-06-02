package vn.edu.hcmuaf.fit.artisanMarket.modules.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.Cart;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.entity.CartItem;
import vn.edu.hcmuaf.fit.artisanMarket.modules.cart.domain.repository.CartRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.Order;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.OrderItem;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.OrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentMethod;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository.OrderItemRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository.OrderRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.request.CreateOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.response.OrderResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.service.OrderService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(String username, CreateOrderRequestDTO request) {
        // 1. Lấy Cart của user từ CartRepository
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        // 2. Validate: Cart không rỗng
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống");
        }

        // 3. Validate: Từng sản phẩm còn hoạt động và đủ tồn kho
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' hiện không hoạt động");
            }
            if (!product.isPreOrder()) {
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng trong kho (" + product.getStockQuantity() + ")");
                }
            }
        }

        // 4. Tạo mã đơn hàng (UUID ngắn, unique)
        String orderCode;
        do {
            orderCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderCode(orderCode));

        // 5. Tạo Order entity với status = PENDING
        User user = cart.getUser();
        Order order = Order.builder()
                .user(user)
                .orderCode(orderCode)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingAddress(request.shippingAddress())
                .note(request.note())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 6. Tạo danh sách OrderItem (snapshot giá tại thời điểm đặt) và trừ tồn kho
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // Xác định giá bán tại thời điểm đặt (ưu tiên discountPrice nếu có)
            BigDecimal finalPrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : product.getPrice();
            
            BigDecimal subTotal = finalPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .productPrice(finalPrice)
                    .quantity(cartItem.getQuantity())
                    .subTotal(subTotal)
                    .build();
            orderItems.add(orderItem);

            // Trừ stockQuantity của từng Product
            if (!product.isPreOrder()) {
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            } else {
                if (product.getStockQuantity() != null) {
                    product.setStockQuantity(Math.max(0, product.getStockQuantity() - cartItem.getQuantity()));
                    productRepository.save(product);
                }
            }
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        // Lưu Order (các OrderItem sẽ được cascade lưu theo)
        Order savedOrder = orderRepository.save(order);

        // 7. Xóa sạch giỏ hàng của User
        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponseDTO.fromEntity(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrders(String username) {
        return orderRepository.findByUserUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String username, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Kiểm tra quyền: phải là chủ đơn hàng hoặc ADMIN
        if (user.getRole() != UserRole.ADMIN && !order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        return OrderResponseDTO.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(String username, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Kiểm tra quyền: phải là chủ đơn hàng hoặc ADMIN
        if (user.getRole() != UserRole.ADMIN && !order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        // Kiểm tra trạng thái: Người dùng chỉ được hủy đơn ở trạng thái PENDING hoặc CONFIRMED
        if (user.getRole() != UserRole.ADMIN) {
            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
                throw new RuntimeException("Đơn hàng không thể hủy ở trạng thái hiện tại (" + order.getStatus() + ")");
            }
        } else {
            // ADMIN không thể hủy đơn đã giao thành công, đã hủy hoặc hoàn tiền
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
                throw new RuntimeException("Đơn hàng không thể hủy ở trạng thái hiện tại (" + order.getStatus() + ")");
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }

        // Hoàn lại stockQuantity cho từng OrderItem
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                if (!product.isPreOrder()) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                } else {
                    if (product.getStockQuantity() != null) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponseDTO.fromEntity(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrdersAdmin() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatusAdmin(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == status) {
            return OrderResponseDTO.fromEntity(order);
        }

        // Nếu chuyển sang CANCELLED, thực hiện hoàn lại tồn kho
        if (status == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    if (!product.isPreOrder()) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    } else {
                        if (product.getStockQuantity() != null) {
                            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                            productRepository.save(product);
                        }
                    }
                }
            }
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
            }
        }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return OrderResponseDTO.fromEntity(savedOrder);
    }
}
