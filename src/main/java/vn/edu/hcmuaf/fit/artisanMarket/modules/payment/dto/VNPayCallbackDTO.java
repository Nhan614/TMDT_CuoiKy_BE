package vn.edu.hcmuaf.fit.artisanMarket.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayCallbackDTO {
    private String responseCode;
    private String txnRef;
    private String secureHash;
    private Map<String, String> queryParams;
}
