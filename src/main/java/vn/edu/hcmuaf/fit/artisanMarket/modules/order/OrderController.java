package vn.edu.hcmuaf.fit.artisanMarket.modules.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.OrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.request.CreateOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.dto.response.OrderResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/orders")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderResponseDTO response = orderService.createOrder(username, request);
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", response));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrders() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OrderResponseDTO> response = orderService.getOrders(username);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", response));
    }

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderResponseDTO response = orderService.getOrderById(username, id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết đơn hàng thành công", response));
    }

    @PatchMapping("/api/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelOrder(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderResponseDTO response = orderService.cancelOrder(username, id);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", response));
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getAllOrdersAdmin() {
        List<OrderResponseDTO> response = orderService.getAllOrdersAdmin();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách đơn hàng thành công (Admin)", response));
    }

    @PatchMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatusAdmin(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        OrderResponseDTO response = orderService.updateOrderStatusAdmin(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công (Admin)", response));
    }
}
