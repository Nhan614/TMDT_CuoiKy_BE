package vn.edu.hcmuaf.fit.artisanMarket.modules.payment;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.dto.VNPayCreatePaymentDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.dto.VNPayPaymentResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.payment.service.PaymentService;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/payment/vnpay/create")
    public ResponseEntity<ApiResponse<VNPayPaymentResponseDTO>> createPaymentUrl(
            @Valid @RequestBody VNPayCreatePaymentDTO request,
            HttpServletRequest servletRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String paymentUrl = paymentService.createPaymentUrl(username, request, servletRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo link thanh toán VNPay thành công",
                new VNPayPaymentResponseDTO(paymentUrl)
        ));
    }

    @GetMapping("/api/payment/vnpay/callback")
    public ResponseEntity<Void> callback(@RequestParam Map<String, String> queryParams) {
        String redirectUrl = paymentService.processCallback(queryParams);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
