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
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.Product;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VNPayConfig vnpayConfig;

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

        // 3. Build tham số VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        
        // vnp_Amount nhận số tiền nhân 100
        BigDecimal amount = order.getTotalAmount().multiply(new BigDecimal(100));
        vnpParams.put("vnp_Amount", String.valueOf(amount.toBigInteger()));
        vnpParams.put("vnp_CurrCode", "VND");
        
        // Transaction reference
        vnpParams.put("vnp_TxnRef", order.getOrderCode());
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        
        // Client IP
        String ipAddr = getIpAddress(servletRequest);
        vnpParams.put("vnp_IpAddr", ipAddr);

        // Created Date (GMT+7)
        ZonedDateTime nowGmt7 = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", nowGmt7.format(formatter));

        // Expire Date (+15 phút, theo spec VNPay)
        ZonedDateTime expireGmt7 = nowGmt7.plusMinutes(15);
        vnpParams.put("vnp_ExpireDate", expireGmt7.format(formatter));

        // 4. Sắp xếp tham số theo alphabet
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        // 5. Tạo chuỗi query và dữ liệu hash
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Hash data: encode theo US_ASCII (đúng spec VNPay)
                hashData.append(fieldName);
                hashData.append("=");
                hashData.append(encodeAscii(fieldValue));

                // Query string: encode theo UTF-8
                query.append(encodeUtf8(fieldName));
                query.append("=");
                query.append(encodeUtf8(fieldValue));

                if (itr.hasNext()) {
                    query.append("&");
                    hashData.append("&");
                }
            }
        }

        // 6. Tạo HMAC-SHA512 checksum
        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());

        // 7. Kết hợp thành URL hoàn chỉnh
        String paymentUrl = vnpayConfig.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;
        log.info("Tạo link thanh toán VNPay thành công cho đơn hàng: {}, URL: {}", order.getOrderCode(), paymentUrl);
        return paymentUrl;
    }

    @Override
    @Transactional
    public String processCallback(Map<String, String> queryParams) {
        log.info("Nhận callback từ VNPay: {}", queryParams);

        // 1. Tách vnp_SecureHash ra khỏi params
        String vnpSecureHash = queryParams.get("vnp_SecureHash");

        // 2. Tính lại checksum từ các params còn lại
        List<String> fieldNames = new ArrayList<>(queryParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = queryParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()
                    && !"vnp_SecureHash".equals(fieldName)
                    && !"vnp_SecureHashType".equals(fieldName)) {

                if (hashData.length() > 0) {
                    hashData.append("&");
                }
                hashData.append(fieldName);
                hashData.append("=");
                // Spring đã URL-decode query params rồi, encode lại theo US_ASCII để verify đúng checksum
                hashData.append(encodeAscii(fieldValue));
            }
        }

        String calculatedHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());

        // 3. So sánh checksum
        if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
            log.warn("VNPay callback checksum không hợp lệ! Nhận được: {}, Tính toán: {}", vnpSecureHash, calculatedHash);
            return vnpayConfig.getFrontendReturnUrl() + "?status=failed&message=InvalidChecksum";
        }

        // 4. Tìm Order theo vnp_TxnRef (= orderCode)
        String orderCode = queryParams.get("vnp_TxnRef");
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElse(null);

        if (order == null) {
            log.error("Không tìm thấy đơn hàng tương ứng với mã VNPay TxnRef: {}", orderCode);
            return vnpayConfig.getFrontendReturnUrl() + "?status=failed&message=OrderNotFound";
        }

        // 5. Kiểm tra vnp_ResponseCode
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

    /**
     * Encode theo US_ASCII — dùng cho hashData (đúng spec VNPay).
     */
    private String encodeAscii(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    /**
     * Encode theo UTF-8 — dùng cho query string URL.
     */
    private String encodeUtf8(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Lỗi sinh checksum HMAC-SHA512 cho VNPay", ex);
            throw new RuntimeException("Lỗi sinh signature cho VNPay", ex);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }
}
