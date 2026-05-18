package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;
import java.time.LocalDate;

public record ArtisanDetailResponse(
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
        LocalDate startedCraftingDate
) {
    public static ArtisanDetailResponse fromEntity(Artisan artisan) {
        return new ArtisanDetailResponse(
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
                artisan.getStartedCraftingDate()
        );
    }
}