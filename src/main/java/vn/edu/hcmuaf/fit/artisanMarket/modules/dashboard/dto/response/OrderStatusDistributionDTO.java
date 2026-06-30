package vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusDistributionDTO {
    private String status;
    private long count;
}
