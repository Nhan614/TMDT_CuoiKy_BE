package vn.edu.hcmuaf.fit.artisanMarket.modules.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.dto.VNPayCreatePaymentDTO;

import java.util.Map;

public interface PaymentService {
    String createPaymentUrl(String username, VNPayCreatePaymentDTO request, HttpServletRequest servletRequest);
    String processCallback(Map<String, String> queryParams);
}
