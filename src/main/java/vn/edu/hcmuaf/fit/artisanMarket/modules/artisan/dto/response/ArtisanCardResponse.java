package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response;

import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.model.Artisan;

public record ArtisanCardResponse(
        Long id,
        String name,
        String tag,
        String image,
        Double rating,
        String quote,
        String experience, // Khớp với artisan.experience ("8 năm")
        String orders,     // Khớp với artisan.orders ("1,200+")
        Boolean featured,
        String skillValue  // Trả về "AMIGURUMI", "DAN_MOC" để phục vụ bộ lọc Filter tiếng Anh/Việt
) {
    public static ArtisanCardResponse fromEntity(Artisan artisan) {
        return new ArtisanCardResponse(
                artisan.getId(),
                artisan.getName(),
                artisan.getSkill().getDisplayName(), // Bê chữ Tag "Bậc thầy Amigurumi" hoặc tên Skill vào đây
                artisan.getImage(),
                artisan.getRating(),
                artisan.getQuote(),
                artisan.getExperienceYears() + " năm",
                formatOrders(artisan.getTotalOrders()),
                artisan.getFeatured(),
                artisan.getSkill().name()
        );
    }

    private static String formatOrders(Integer totalOrders) {
        if (totalOrders == null || totalOrders == 0) return "0";
        if (totalOrders >= 1000) {
            return String.format("%,d+", totalOrders); // Format 1200 thành "1,200+" giống Mock Data của bạn
        }
        return String.valueOf(totalOrders);
    }
}