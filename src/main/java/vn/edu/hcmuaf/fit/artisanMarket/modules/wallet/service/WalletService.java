package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request.ReviewWithdrawalRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request.WithdrawalRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WalletBalanceResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WalletTransactionResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WithdrawalRequestResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WithdrawalStatus;

public interface WalletService {
    WalletBalanceResponseDTO getWalletBalance(String username);
    Page<WalletTransactionResponseDTO> getWalletTransactions(String username, int page, int size);
    WithdrawalRequestResponseDTO requestWithdrawal(String username, WithdrawalRequestDTO dto);
    Page<WithdrawalRequestResponseDTO> getWithdrawalRequests(String username, int page, int size);
    
    // Admin
    Page<WithdrawalRequestResponseDTO> getWithdrawalRequestsForAdmin(WithdrawalStatus status, int page, int size);
    WithdrawalRequestResponseDTO reviewWithdrawalRequest(Long id, String adminUsername, ReviewWithdrawalRequestDTO dto);
}
