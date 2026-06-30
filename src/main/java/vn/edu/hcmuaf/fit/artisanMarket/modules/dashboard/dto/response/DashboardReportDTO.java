package vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardReportDTO {
    private DashboardOverviewDTO overview;
    private List<RevenueStatisticsDTO> revenueChart;
    private List<TopSellingProductDTO> topProducts;
    private List<TopArtisanDTO> topArtisans;
    private List<OrderStatusDistributionDTO> orderStatusDistribution;
    private List<OrderStatusDistributionDTO> customOrderStatusDistribution;
}
