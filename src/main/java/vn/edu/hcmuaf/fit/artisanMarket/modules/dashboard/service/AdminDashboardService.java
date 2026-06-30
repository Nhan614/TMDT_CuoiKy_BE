package vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.service;

import vn.edu.hcmuaf.fit.artisanMarket.modules.dashboard.dto.response.DashboardReportDTO;

import java.time.LocalDate;

public interface AdminDashboardService {
    DashboardReportDTO getDashboardReport(LocalDate startDate, LocalDate endDate);
}
