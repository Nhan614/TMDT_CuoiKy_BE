package vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopArtisanDTO {
    private String artisanName;
    private BigDecimal revenue;
    private long orderCount;
}
