package vn.edu.hcmuaf.fit.artisanMarket.modules.payment.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.config.VNPayConfig;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class VNPayUtil {

    private final VNPayConfig vnpayConfig;

    /**
     * Build standard VNPay payment URL.
     */
    public String createPaymentUrl(String txnRef, BigDecimal totalAmount, String orderInfo, HttpServletRequest servletRequest) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        
        // Amount must be multiplied by 100 for VNPay (e.g. 10000 VND becomes 1000000)
        BigDecimal amount = totalAmount.multiply(new BigDecimal(100));
        vnpParams.put("vnp_Amount", String.valueOf(amount.toBigInteger()));
        vnpParams.put("vnp_CurrCode", "VND");
        
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        
        String ipAddr = getIpAddress(servletRequest);
        vnpParams.put("vnp_IpAddr", ipAddr);

        // Created Date (GMT+7)
        ZonedDateTime nowGmt7 = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", nowGmt7.format(formatter));

        // Expire Date (+15 minutes)
        ZonedDateTime expireGmt7 = nowGmt7.plusMinutes(15);
        vnpParams.put("vnp_ExpireDate", expireGmt7.format(formatter));

        // Sort params alphabetically
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Hash data: encode as US_ASCII (according to VNPay spec)
                hashData.append(fieldName);
                hashData.append("=");
                hashData.append(encodeAscii(fieldValue));

                // Query string: encode as UTF-8
                query.append(encodeUtf8(fieldName));
                query.append("=");
                query.append(encodeUtf8(fieldValue));

                if (itr.hasNext()) {
                    query.append("&");
                    hashData.append("&");
                }
            }
        }

        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());

        return vnpayConfig.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;
    }

    /**
     * Verify callback signature.
     */
    public boolean verifyChecksum(Map<String, String> queryParams) {
        String vnpSecureHash = queryParams.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            return false;
        }

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
                hashData.append(encodeAscii(fieldValue));
            }
        }

        String calculatedHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

    public String encodeAscii(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public String encodeUtf8(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public String hmacSHA512(String key, String data) {
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

    public String getIpAddress(HttpServletRequest request) {
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
