package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanCardResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanDetailResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanProfileResponse;

public interface ArtisanService {

    Page<ArtisanCardResponse> getArtisansMarket(String skill, String sortBy, Pageable pageable);
    ArtisanProfileResponse getArtisanProfile(Long id);

    ArtisanDetailResponse getArtisanDetail(Long id);
    void processNewOrder(Long id);
    void processCompleteOrder(Long id);
}