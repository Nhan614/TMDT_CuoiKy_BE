package vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.domain.entity.enums.CustomOrderStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.AcceptCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.CreateCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.request.RejectCustomOrderRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.dto.response.CustomOrderResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.customorder.service.CustomOrderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-orders")
@RequiredArgsConstructor
public class CustomOrderController {

    private final CustomOrderService customOrderService;

    @PostMapping("/upload-reference")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadReference(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tải ảnh mẫu lên thành công",
                customOrderService.uploadReferenceImage(file))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomOrderResponseDTO>> createCustomOrder(
            @Valid @RequestBody CreateCustomOrderRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Tạo yêu cầu gia công thành công",
                customOrderService.createCustomOrder(dto))
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CustomOrderResponseDTO>>> getMyCustomOrders(
            @RequestParam(required = false) CustomOrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách yêu cầu gia công của tôi thành công",
                customOrderService.getMyCustomOrders(status, page, size))
        );
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<CustomOrderResponseDTO>> getMyCustomOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết yêu cầu gia công thành công",
                customOrderService.getMyCustomOrderDetails(id))
        );
    }

    @PatchMapping("/my/{id}/cancel")
    public ResponseEntity<ApiResponse<CustomOrderResponseDTO>> cancelCustomOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hủy yêu cầu gia công thành công",
                customOrderService.cancelCustomOrder(id))
        );
    }

    @GetMapping("/artisan")
    public ResponseEntity<ApiResponse<List<CustomOrderResponseDTO>>> getArtisanCustomOrders(
            @RequestParam(required = false) CustomOrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách yêu cầu gia công nhận được thành công",
                customOrderService.getArtisanCustomOrders(status, page, size))
        );
    }

    @GetMapping("/artisan/{id}")
    public ResponseEntity<ApiResponse<CustomOrderResponseDTO>> getArtisanCustomOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết yêu cầu gia công thành công",
                customOrderService.getArtisanCustomOrderDetails(id))
        );
    }

    @PatchMapping("/artisan/{id}/accept")
    public ResponseEntity<ApiResponse<CustomOrderResponseDTO>> acceptCustomOrder(
            @PathVariable Long id,
            @RequestBody AcceptCustomOrderRequestDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Chấp nhận yêu cầu gia công thành công",
                customOrderService.acceptCustomOrder(id, dto))
        );
    }

    @PatchMapping("/artisan/{id}/reject")
    public ResponseEntity<ApiResponse<CustomOrderResponseDTO>> rejectCustomOrder(
            @PathVariable Long id,
            @Valid @RequestBody RejectCustomOrderRequestDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Từ chối yêu cầu gia công thành công",
                customOrderService.rejectCustomOrder(id, dto))
        );
    }
}
