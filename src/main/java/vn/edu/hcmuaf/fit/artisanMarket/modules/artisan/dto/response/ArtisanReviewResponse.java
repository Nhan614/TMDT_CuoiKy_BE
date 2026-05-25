package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response;

import java.time.LocalDate;

public record ArtisanReviewResponse(
        Long id,
        String customerName,
        String customerAvatar,
        Double rating,
        String comment,
        LocalDate createdAt
) {}