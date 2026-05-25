package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import java.time.LocalDate;
import java.util.List;

public record ArtisanProfileResponse(
        Long id,
        String name,
        String tag,
        String image,
        Double rating,
        String quote,
        Integer totalOrders,
        Integer activeOrdersCount,
        String skillValue,
        String status,
        LocalDate startedCraftingDate,
        String experience,

        List<ArtisanProductResponse> portfolioProducts,
        List<ArtisanReviewResponse> reviews
) {
    public static ArtisanProfileResponse fromEntity(
            Artisan artisan,
            List<ArtisanProductResponse> products,
            List<ArtisanReviewResponse> reviews
    ) {
        return new ArtisanProfileResponse(
                artisan.getId(),
                artisan.getName(),
                artisan.getSkill().getDisplayName(),
                artisan.getImage(),
                artisan.getRating(),
                artisan.getQuote(),
                artisan.getTotalOrders(),
                artisan.getActiveOrdersCount(),
                artisan.getSkill().name(),
                artisan.getStatus().name(),
                artisan.getStartedCraftingDate(),
                artisan.getExperienceYears() + " năm",
                products,
                reviews
        );
    }
}