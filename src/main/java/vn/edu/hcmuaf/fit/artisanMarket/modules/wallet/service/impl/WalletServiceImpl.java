package vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.User;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.entity.enums.UserRole;
import vn.edu.hcmuaf.fit.artisanMarket.modules.user.domain.repository.UserRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.WalletTransaction;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.WithdrawalRequest;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WalletTransactionType;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.entity.enums.WithdrawalStatus;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.repository.WalletTransactionRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.domain.repository.WithdrawalRequestRepository;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request.ReviewWithdrawalRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.request.WithdrawalRequestDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WalletBalanceResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WalletTransactionResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.dto.response.WithdrawalRequestResponseDTO;
import vn.edu.hcmuaf.fit.artisanMarket.modules.wallet.service.WalletService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    private static final BigDecimal THRESHOLD = new BigDecimal("5000000"); // 5 triệu VNĐ

    @Override
    public WalletBalanceResponseDTO getWalletBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        return new WalletBalanceResponseDTO(user.getBalance(), "VND");
    }

    @Override
    public Page<WalletTransactionResponseDTO> getWalletTransactions(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        int pageIndex = page > 0 ? page - 1 : 0;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return walletTransactionRepository.findByUserId(user.getId(), pageRequest)
                .map(WalletTransactionResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public WithdrawalRequestResponseDTO requestWithdrawal(String username, WithdrawalRequestDTO dto) {
        log.info("Người dùng {} yêu cầu rút tiền với số tiền: {}", username, dto.amount());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (user.getRole() != UserRole.ARTISAN) {
            throw new IllegalStateException("Chỉ tài khoản thợ thủ công mới có thể thực hiện rút tiền");
        }

        BigDecimal pendingWithdrawals = withdrawalRequestRepository.sumPendingWithdrawalAmountByUserId(user.getId());
        BigDecimal availableBalance = user.getBalance().subtract(pendingWithdrawals);

        if (availableBalance.compareTo(dto.amount()) < 0) {
            throw new IllegalArgumentException("Số dư khả dụng không đủ để thực hiện yêu cầu. " +
                    "Số dư hiện tại: " + user.getBalance() + " VNĐ, " +
                    "Đang chờ duyệt: " + pendingWithdrawals + " VNĐ. " +
                    "Số dư khả dụng: " + availableBalance + " VNĐ.");
        }

        WithdrawalRequest request = WithdrawalRequest.builder()
                .user(user)
                .amount(dto.amount())
                .bankName(dto.bankName())
                .accountNumber(dto.accountNumber())
                .accountHolder(dto.accountHolder().toUpperCase())
                .build();

        if (dto.amount().compareTo(THRESHOLD) < 0) {
            // Tự động duyệt dưới 5 triệu VNĐ
            request.setStatus(WithdrawalStatus.COMPLETED);
            request = withdrawalRequestRepository.save(request);

            // Trừ số dư trực tiếp
            user.setBalance(user.getBalance().subtract(dto.amount()));
            userRepository.save(user);

            // Ghi lịch sử giao dịch
            WalletTransaction transaction = WalletTransaction.builder()
                    .user(user)
                    .type(WalletTransactionType.DEBIT)
                    .amount(dto.amount())
                    .balanceAfter(user.getBalance())
                    .description("Rút tiền tự động")
                    .build();
            walletTransactionRepository.save(transaction);

            log.info("Yêu cầu rút tiền tự động thành công cho thợ {}", username);
        } else {
            // Trên 5 triệu cần Admin duyệt
            request.setStatus(WithdrawalStatus.PENDING);
            request = withdrawalRequestRepository.save(request);
            log.info("Yêu cầu rút tiền của thợ {} cần admin duyệt, ID yêu cầu: {}", username, request.getId());
        }

        return WithdrawalRequestResponseDTO.fromEntity(request);
    }

    @Override
    public Page<WithdrawalRequestResponseDTO> getWithdrawalRequests(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        int pageIndex = page > 0 ? page - 1 : 0;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return withdrawalRequestRepository.findByUserId(user.getId(), pageRequest)
                .map(WithdrawalRequestResponseDTO::fromEntity);
    }

    @Override
    public Page<WithdrawalRequestResponseDTO> getWithdrawalRequestsForAdmin(WithdrawalStatus status, int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<WithdrawalRequest> requests;
        if (status == null) {
            requests = withdrawalRequestRepository.findAll(pageRequest);
        } else {
            requests = withdrawalRequestRepository.findByStatus(status, pageRequest);
        }

        return requests.map(WithdrawalRequestResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public WithdrawalRequestResponseDTO reviewWithdrawalRequest(Long id, String adminUsername, ReviewWithdrawalRequestDTO dto) {
        log.info("Admin {} đánh giá yêu cầu rút tiền ID: {} với hành động: {}", adminUsername, id, dto.action());

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản admin"));

        WithdrawalRequest request = withdrawalRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu rút tiền"));

        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu rút tiền này đã được xử lý hoặc không ở trạng thái chờ duyệt (PENDING)");
        }

        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setNote(dto.note());

        if ("APPROVE".equalsIgnoreCase(dto.action())) {
            User user = request.getUser();
            if (user.getBalance().compareTo(request.getAmount()) < 0) {
                throw new IllegalStateException("Số dư hiện tại của thợ thủ công không đủ để hoàn tất việc rút tiền này");
            }

            // Trừ số dư của thợ
            user.setBalance(user.getBalance().subtract(request.getAmount()));
            userRepository.save(user);

            // Ghi lịch sử giao dịch
            WalletTransaction transaction = WalletTransaction.builder()
                    .user(user)
                    .type(WalletTransactionType.DEBIT)
                    .amount(request.getAmount())
                    .balanceAfter(user.getBalance())
                    .description("Rút tiền được duyệt bởi Admin")
                    .build();
            walletTransactionRepository.save(transaction);

            request.setStatus(WithdrawalStatus.APPROVED);
            log.info("Admin {} đã DUYỆT yêu cầu rút tiền của thợ {}", adminUsername, user.getUsername());
        } else {
            request.setStatus(WithdrawalStatus.REJECTED);
            log.info("Admin {} đã TỪ CHỐI yêu cầu rút tiền của thợ {}", adminUsername, request.getUser().getUsername());
        }

        request = withdrawalRequestRepository.save(request);
        return WithdrawalRequestResponseDTO.fromEntity(request);
    }
}
