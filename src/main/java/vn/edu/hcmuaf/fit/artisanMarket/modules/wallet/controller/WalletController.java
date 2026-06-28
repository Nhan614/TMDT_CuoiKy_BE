package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.artisanMarket.common.ApiResponse;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request.WithdrawalRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WalletBalanceResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WalletTransactionResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WithdrawalRequestResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTISAN')")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletBalanceResponseDTO>> getWalletBalance() {
        String username = getCurrentUsername();
        WalletBalanceResponseDTO balance = walletService.getWalletBalance(username);
        return ResponseEntity.ok(ApiResponse.success("Lấy số dư ví thành công", balance));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponseDTO>>> getWalletTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = getCurrentUsername();
        Page<WalletTransactionResponseDTO> transactions = walletService.getWalletTransactions(username, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch sử giao dịch thành công", transactions));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalRequestResponseDTO>> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO dto
    ) {
        String username = getCurrentUsername();
        WithdrawalRequestResponseDTO response = walletService.requestWithdrawal(username, dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo yêu cầu rút tiền thành công", response));
    }

    @GetMapping("/withdrawals")
    public ResponseEntity<ApiResponse<List<WithdrawalRequestResponseDTO>>> getWithdrawalRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = getCurrentUsername();
        Page<WithdrawalRequestResponseDTO> withdrawals = walletService.getWithdrawalRequests(username, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu rút tiền thành công", withdrawals));
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
