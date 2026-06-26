package vn.edu.hcmuaf.fit.artisanMarket.modules.payment.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.Order;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.OrderItem;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.OrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.enums.PaymentStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository.OrderRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.config.VNPayConfig;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.dto.VNPayCreatePaymentDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.service.PaymentService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service.CustomOrderService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.util.VNPayUtil;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.model.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VNPayConfig vnpayConfig;
    private final VNPayUtil vnpayUtil;
    private final CustomOrderService customOrderService;

    @Override
    @Transactional(readOnly = true)
    public String createPaymentUrl(String username, VNPayCreatePaymentDTO request, HttpServletRequest servletRequest) {
        log.info("Bắt đầu tạo link thanh toán VNPay cho đơn hàng ID: {}", request.orderId());

        // 1. Tìm Order theo orderId, validate status = PENDING
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng đã được xử lý hoặc hủy bỏ trước đó");
        }

        // 2. Validate Order thuộc về user đang đăng nhập
        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thanh toán cho đơn hàng này");
        }

        // 3. Sử dụng VNPayUtil để tạo payment url
        String txnRef = order.getOrderCode();
        String orderInfo = "Thanh toan don hang #" + order.getOrderCode();
        return vnpayUtil.createPaymentUrl(txnRef, order.getTotalAmount(), orderInfo, servletRequest);
    }

    @Override
    @Transactional
    public String processCallback(Map<String, String> queryParams) {
        log.info("Nhận callback từ VNPay: {}", queryParams);

        // 1. Xác minh checksum thông qua VNPayUtil
        if (!vnpayUtil.verifyChecksum(queryParams)) {
            log.warn("VNPay callback checksum không hợp lệ!");
            return vnpayConfig.getFrontendReturnUrl() + "?status=failed&message=InvalidChecksum";
        }

        // 2. Phân biệt loại đơn hàng qua vnp_TxnRef prefix
        String txnRef = queryParams.get("vnp_TxnRef");
        if (txnRef != null && txnRef.startsWith("CO-")) {
            // Đây là callback cho Custom Order — ủy quyền xử lý sang CustomOrderService
            log.info("Routing VNPay callback cho Custom Order, TxnRef: {}", txnRef);
            return customOrderService.processPaymentCallback(queryParams);
        }

        // 3. Tìm Order thường theo vnp_TxnRef (= orderCode)
        String orderCode = txnRef;
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElse(null);

        if (order == null) {
            log.error("Không tìm thấy đơn hàng tương ứng với mã VNPay TxnRef: {}", orderCode);
            return vnpayConfig.getFrontendReturnUrl() + "?status=failed&message=OrderNotFound";
        }

        // 4. Kiểm tra vnp_ResponseCode
        String responseCode = queryParams.get("vnp_ResponseCode");

        // Idempotency: Kiểm tra nếu đơn hàng đã được xử lý thanh toán thành công rồi
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Đơn hàng {} đã được thanh toán thành công trước đó (bỏ qua cập nhật)", orderCode);
            return vnpayConfig.getFrontendReturnUrl() + "?status=success&orderCode=" + orderCode;
        }

        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            log.info("Thanh toán thành công qua VNPay cho đơn hàng: {}", orderCode);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);

            return vnpayConfig.getFrontendReturnUrl() + "?status=success&orderCode=" + orderCode;
        } else {
            // Thanh toán thất bại hoặc người dùng hủy
            log.warn("Thanh toán thất bại qua VNPay cho đơn hàng: {}, ResponseCode: {}", orderCode, responseCode);

            // Chỉ cập nhật trạng thái nếu đơn hàng chưa bị hủy/thất bại trước đó để tránh duplicate
            if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);

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
                orderRepository.save(order);
            }

            return vnpayConfig.getFrontendReturnUrl() + "?status=failed&orderCode=" + orderCode + "&responseCode=" + responseCode;
        }
    }
}
