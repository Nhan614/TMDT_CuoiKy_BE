package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response;

public record ArtisanProductResponse(
        Long id,
        String name,
        String image,
        Double price,
        Integer totalSales
) {}