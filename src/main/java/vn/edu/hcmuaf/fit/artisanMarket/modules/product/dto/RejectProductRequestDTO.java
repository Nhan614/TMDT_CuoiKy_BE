package vn.edu.hcmuaf.fit.artisanMarket.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectProductRequestDTO(
    @NotBlank(message = "Lý do từ chối không được để trống")
    String reason
) {}
