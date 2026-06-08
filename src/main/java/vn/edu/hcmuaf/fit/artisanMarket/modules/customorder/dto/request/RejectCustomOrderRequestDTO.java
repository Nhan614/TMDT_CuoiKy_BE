package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectCustomOrderRequestDTO(
    @NotBlank(message = "Lý do từ chối không được để trống") 
    String artisanNote
) {}
