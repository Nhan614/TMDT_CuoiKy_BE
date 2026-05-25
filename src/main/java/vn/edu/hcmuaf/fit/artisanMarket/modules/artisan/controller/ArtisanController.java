package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanCardResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanProfileResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.service.ArtisanService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArtisanController {

    private final ArtisanService artisanService;

    @GetMapping("/api/artisans")
    public ResponseEntity<ApiResponse<List<ArtisanCardResponse>>> getMarketplace(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ArtisanCardResponse> pageResult = artisanService.getArtisansMarket(skill, sortBy, pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nghệ nhân thành công", pageResult));
    }
    @GetMapping("/api/artisans/{id}")
    public ResponseEntity<ApiResponse<ArtisanProfileResponse>> getArtisanProfile(@PathVariable Long id) {
        try {
            ArtisanProfileResponse profile = artisanService.getArtisanProfile(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết nghệ nhân thành công", profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}