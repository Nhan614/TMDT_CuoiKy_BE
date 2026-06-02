package vn.edu.hcmuaf.fit.artisanMarket.modules.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayConfig {
    private String tmnCode;
    private String hashSecret;
    private String url;
    private String returnUrl;        // URL gửi cho VNPay (backend callback)
    private String frontendReturnUrl; // URL redirect về frontend sau khi xử lý
    private String apiUrl;
}
