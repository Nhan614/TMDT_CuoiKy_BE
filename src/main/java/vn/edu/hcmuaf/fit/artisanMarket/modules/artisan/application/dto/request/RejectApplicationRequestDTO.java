package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectApplicationRequestDTO(
        @NotBlank(message = "Lý do từ chối không được để trống")
        String reason
) {}
