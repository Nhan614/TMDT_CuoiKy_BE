package vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.repository.ArtisanRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.CustomOrder;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.repository.CustomOrderRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.dto.response.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.service.AdminDashboardService;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.entity.Order;
import vn.edu.hcmuaf.fit.artisanMarket.modules.order.domain.repository.OrderRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.product.domain.repository.ProductRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderRepository orderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ArtisanRepository artisanRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardReportDTO getDashboardReport(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }

        // 1. Overview
        DashboardOverviewDTO overview = getOverviewMetrics();

        // 2. Revenue Chart
        List<RevenueStatisticsDTO> revenueChart = getRevenueChartData(startDate, endDate);

        // 3. Top Products
        List<TopSellingProductDTO> topProducts = getTopSellingProducts();

        // 4. Top Artisans
        List<TopArtisanDTO> topArtisans = getTopArtisans();

        // 5. Order Status Distribution (Regular)
        List<OrderStatusDistributionDTO> orderStatusDistribution = getOrderStatusDistribution();

        // 6. Custom Order Status Distribution
        List<OrderStatusDistributionDTO> customOrderStatusDistribution = getCustomOrderStatusDistribution();

        return DashboardReportDTO.builder()
                .overview(overview)
                .revenueChart(revenueChart)
                .topProducts(topProducts)
                .topArtisans(topArtisans)
                .orderStatusDistribution(orderStatusDistribution)
                .customOrderStatusDistribution(customOrderStatusDistribution)
                .build();
    }

    private DashboardOverviewDTO getOverviewMetrics() {
        BigDecimal totalRegular = orderRepository.sumTotalPaidRevenue();
        if (totalRegular == null) totalRegular = BigDecimal.ZERO;

        BigDecimal totalCustom = customOrderRepository.sumTotalPaidCustomRevenue();
        if (totalCustom == null) totalCustom = BigDecimal.ZERO;

        BigDecimal totalRevenue = totalRegular.add(totalCustom);

        long regularOrders = orderRepository.count();
        long customOrders = customOrderRepository.count();
        long totalOrders = regularOrders + customOrders;

        long totalProducts = productRepository.count();
        long totalArtisans = artisanRepository.count();
        long totalCustomers = userRepository.countByRole(UserRole.USER);

        return DashboardOverviewDTO.builder()
                .totalRevenue(totalRevenue)
                .totalRegularRevenue(totalRegular)
                .totalCustomRevenue(totalCustom)
                .totalOrders(totalOrders)
                .totalRegularOrders(regularOrders)
                .totalCustomOrders(customOrders)
                .totalProducts(totalProducts)
                .totalArtisans(totalArtisans)
                .totalCustomers(totalCustomers)
                .build();
    }

    private List<RevenueStatisticsDTO> getRevenueChartData(LocalDate startDate, LocalDate endDate) {
        Map<String, RevenueStatisticsDTO> chartMap = new LinkedHashMap<>();
        
        // Generate continuous days
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.toString();
            chartMap.put(dateStr, RevenueStatisticsDTO.builder()
                    .date(dateStr)
                    .regularRevenue(BigDecimal.ZERO)
                    .customRevenue(BigDecimal.ZERO)
                    .totalRevenue(BigDecimal.ZERO)
                    .orderCount(0L)
                    .build());
            current = current.plusDays(1);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Fetch regular orders
        List<Order> regularOrders = orderRepository.findPaidOrdersBetweenDates(startDateTime, endDateTime);
        for (Order order : regularOrders) {
            String dateStr = order.getCreatedAt().toLocalDate().toString();
            RevenueStatisticsDTO dto = chartMap.get(dateStr);
            if (dto != null) {
                BigDecimal orderAmt = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                dto.setRegularRevenue(dto.getRegularRevenue().add(orderAmt));
                dto.setTotalRevenue(dto.getTotalRevenue().add(orderAmt));
                dto.setOrderCount(dto.getOrderCount() + 1);
            }
        }

        // Fetch custom orders
        List<CustomOrder> customOrders = customOrderRepository.findPaidCustomOrdersBetweenDates(startDateTime, endDateTime);
        for (CustomOrder co : customOrders) {
            LocalDateTime paymentTime = co.getPaymentAt() != null ? co.getPaymentAt() : co.getCreatedAt();
            String dateStr = paymentTime.toLocalDate().toString();
            RevenueStatisticsDTO dto = chartMap.get(dateStr);
            if (dto != null) {
                BigDecimal quoteAmt = co.getQuotedPrice() != null ? co.getQuotedPrice() : BigDecimal.ZERO;
                dto.setCustomRevenue(dto.getCustomRevenue().add(quoteAmt));
                dto.setTotalRevenue(dto.getTotalRevenue().add(quoteAmt));
                dto.setOrderCount(dto.getOrderCount() + 1);
            }
        }

        return new ArrayList<>(chartMap.values());
    }

    private List<TopSellingProductDTO> getTopSellingProducts() {
        List<Object[]> rawList = orderRepository.findTopSellingProductsRaw(PageRequest.of(0, 5));
        return rawList.stream().map(row -> TopSellingProductDTO.builder()
                .productId((Long) row[0])
                .productName((String) row[1])
                .thumbnailUrl((String) row[2])
                .quantitySold(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                .totalRevenue(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .build()
        ).collect(Collectors.toList());
    }

    private List<TopArtisanDTO> getTopArtisans() {
        Map<String, TopArtisanDTO> combinedMap = new HashMap<>();

        // 1. Regular orders top artisans
        List<Object[]> regularArtisans = orderRepository.findTopArtisansFromRegularOrdersRaw(PageRequest.of(0, 10));
        for (Object[] row : regularArtisans) {
            String name = (String) row[0];
            if (name == null || name.trim().isEmpty()) {
                name = "Unknown Artisan";
            }
            BigDecimal revenue = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;

            combinedMap.put(name, TopArtisanDTO.builder()
                    .artisanName(name)
                    .revenue(revenue)
                    .orderCount(count)
                    .build());
        }

        // 2. Custom orders top artisans
        List<Object[]> customArtisans = customOrderRepository.findTopArtisansFromCustomOrdersRaw(PageRequest.of(0, 10));
        for (Object[] row : customArtisans) {
            String name = (String) row[0];
            if (name == null || name.trim().isEmpty()) {
                name = "Unknown Artisan";
            }
            BigDecimal revenue = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;

            if (combinedMap.containsKey(name)) {
                TopArtisanDTO existing = combinedMap.get(name);
                existing.setRevenue(existing.getRevenue().add(revenue));
                existing.setOrderCount(existing.getOrderCount() + count);
            } else {
                combinedMap.put(name, TopArtisanDTO.builder()
                        .artisanName(name)
                        .revenue(revenue)
                        .orderCount(count)
                        .build());
            }
        }

        // Sort by revenue descending and limit to top 5
        return combinedMap.values().stream()
                .sorted(Comparator.comparing(TopArtisanDTO::getRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<OrderStatusDistributionDTO> getOrderStatusDistribution() {
        List<Object[]> rawList = orderRepository.countOrdersByStatus();
        return rawList.stream().map(row -> OrderStatusDistributionDTO.builder()
                .status(row[0] != null ? row[0].toString() : "UNKNOWN")
                .count(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                .build()
        ).collect(Collectors.toList());
    }

    private List<OrderStatusDistributionDTO> getCustomOrderStatusDistribution() {
        List<Object[]> rawList = customOrderRepository.countCustomOrdersByStatus();
        return rawList.stream().map(row -> OrderStatusDistributionDTO.builder()
                .status(row[0] != null ? row[0].toString() : "UNKNOWN")
                .count(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                .build()
        ).collect(Collectors.toList());
    }
}
