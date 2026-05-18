package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.service;

import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanCardResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanDetailResponse;

import java.util.List;

public interface ArtisanService {
    // Hàm mới gộp cả Filter nghiệp vụ và Sắp xếp cho Chợ nghệ nhân
    List<ArtisanCardResponse> getArtisansMarket(String skill, String sortBy);

    ArtisanDetailResponse getArtisanDetail(Long id);
    void processNewOrder(Long id);
    void processCompleteOrder(Long id);
}