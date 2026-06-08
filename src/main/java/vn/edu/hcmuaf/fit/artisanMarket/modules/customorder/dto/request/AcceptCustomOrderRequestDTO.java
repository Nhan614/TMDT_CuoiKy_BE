package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request;

import java.math.BigDecimal;

public record AcceptCustomOrderRequestDTO(
    BigDecimal quotedPrice,
    String artisanNote
) {}
