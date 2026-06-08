package vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.domain.entity.enums.ApplicationStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.request.RejectApplicationRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.request.SubmitApplicationRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.dto.response.ArtisanApplicationResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.artisan.application.service.ArtisanApplicationService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artisan-applications")
public class ArtisanApplicationController {

    private final ArtisanApplicationService applicationService;

    @PostMapping("/upload-proof")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProof(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tải ảnh minh chứng thành công",
                applicationService.uploadProofImage(file))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArtisanApplicationResponseDTO>> submitApplication(
            @Valid @RequestBody SubmitApplicationRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Nộp đơn đăng ký thành công. Vui lòng chờ Admin xét duyệt.",
                applicationService.submitApplication(dto))
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<ArtisanApplicationResponseDTO>> getMyApplication() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin đơn đăng ký thành công",
                applicationService.getMyApplication())
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<ArtisanApplicationResponseDTO>>> getAllApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn đăng ký thành công",
                applicationService.getAllApplications(status, page, size))
        );
    }

    @PatchMapping("/admin/{id}/approve")
    public ResponseEntity<ApiResponse<ArtisanApplicationResponseDTO>> approveApplication(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Duyệt đơn đăng ký thành công",
                applicationService.approveApplication(id))
        );
    }

    @PatchMapping("/admin/{id}/reject")
    public ResponseEntity<ApiResponse<ArtisanApplicationResponseDTO>> rejectApplication(
            @PathVariable Long id,
            @Valid @RequestBody RejectApplicationRequestDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Từ chối đơn đăng ký thành công",
                applicationService.rejectApplication(id, dto.reason()))
        );
    }
}
