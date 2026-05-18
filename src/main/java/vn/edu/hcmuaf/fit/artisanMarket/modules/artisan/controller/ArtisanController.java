package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanCardResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.dto.response.ArtisanDetailResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.service.ArtisanService;

import java.util.List;

@RestController
@RequestMapping("/api/artisans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArtisanController {

    private final ArtisanService artisanService;

    @GetMapping
    public ResponseEntity<List<ArtisanCardResponse>> getMarketplace(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false, defaultValue = "rating") String sortBy
    ) {
        List<ArtisanCardResponse> artisans = artisanService.getArtisansMarket(skill, sortBy);
        return ResponseEntity.ok(artisans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtisanDetailResponse> getArtisanDetail(@PathVariable Long id) {
        ArtisanDetailResponse artisanDetail = artisanService.getArtisanDetail(id);
        return ResponseEntity.ok(artisanDetail);
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<String> createOrder(@PathVariable Long id) {
        try {
            artisanService.processNewOrder(id);
            return ResponseEntity.ok("Tiếp nhận yêu cầu đơn hàng thành công!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/orders/complete")
    public ResponseEntity<String> completeOrder(@PathVariable Long id) {
        artisanService.processCompleteOrder(id);
        return ResponseEntity.ok("Cập nhật hoàn thành đơn hàng!");
    }
}