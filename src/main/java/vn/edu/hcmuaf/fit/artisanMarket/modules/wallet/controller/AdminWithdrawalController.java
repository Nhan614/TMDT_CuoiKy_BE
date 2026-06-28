package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WithdrawalStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request.ReviewWithdrawalRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WithdrawalRequestResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/withdrawals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWithdrawalController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WithdrawalRequestResponseDTO>>> getWithdrawalRequests(
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<WithdrawalRequestResponseDTO> requests = walletService.getWithdrawalRequestsForAdmin(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu rút tiền thành công", requests));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<WithdrawalRequestResponseDTO>> reviewWithdrawalRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReviewWithdrawalRequestDTO dto
    ) {
        String adminUsername = getCurrentUsername();
        WithdrawalRequestResponseDTO response = walletService.reviewWithdrawalRequest(id, adminUsername, dto);
        String msg = "APPROVE".equalsIgnoreCase(dto.action()) ? "Đã duyệt yêu cầu rút tiền" : "Đã từ chối yêu cầu rút tiền";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
