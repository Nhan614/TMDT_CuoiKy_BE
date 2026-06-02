package vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums;

public enum OrderStatus {
    PENDING,        // Chờ xác nhận thanh toán
    CONFIRMED,      // Đã thanh toán, chờ xử lý
    PROCESSING,     // Nghệ nhân đang làm
    SHIPPED,        // Đã giao vận chuyển
    DELIVERED,      // Đã giao tới khách
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}
