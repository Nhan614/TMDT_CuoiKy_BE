package vn.edu.hcmuaf.fit.artisanMarket.modules.order.service;

import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.OrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.request.CreateOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(String username, CreateOrderRequestDTO request);
    List<OrderResponseDTO> getOrders(String username);
    OrderResponseDTO getOrderById(String username, Long id);
    OrderResponseDTO cancelOrder(String username, Long id);
    List<OrderResponseDTO> getAllOrdersAdmin();
    OrderResponseDTO updateOrderStatusAdmin(Long id, OrderStatus status);
}
